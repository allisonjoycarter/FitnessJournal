package com.catscoffeeandkitchen.data.workouts.util

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseWithSets
import com.catscoffeeandkitchen.data.workouts.models.RemoteKeys
import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.network.models.*
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import com.catscoffeeandkitchen.data.workouts.models.Exercise as DbExercise

@OptIn(ExperimentalPagingApi::class)
class ExerciseRemoteMediator(
    private val query: String,
    private val muscleFilter: String,
    private val category: String,
    private val service: ExerciseSearchService.Impl,
    private val database: FitnessJournalDb
) : RemoteMediator<Int, ExerciseWithSets>() {

    companion object {
        const val PAGE_SIZE = 20
        const val STARTING_PAGE = 0
    }

    override suspend fun initialize(): InitializeAction {
        // Launch remote refresh as soon as paging starts and do not trigger remote prepend or
        // append until refresh has succeeded. In cases where we don't mind showing out-of-date,
        // cached offline data, we can return SKIP_INITIAL_REFRESH instead to prevent paging
        // triggering remote refresh.
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ExerciseWithSets>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: STARTING_PAGE
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        Timber.d("*** attempting to get result for exercises, page = $page")

        try {
            val searchedExercises = mutableListOf<WgerSearchData>()

            var nextPage: String? = null
            if (query.isNotEmpty()) {
                val searchResult = service.searchExercises(
                    query,
                )

                searchResult.suggestions.forEach { item ->
//                    val exerciseData = service.getExercises(name = item.data.name, limit = 1, offset = 0).results
//                    exerciseData.firstOrNull()?.let { data ->
                        searchedExercises.add(WgerSearchData(
                        id = item.data.id,
                        item.data.baseId,
                        item.data.name,
                        WgerExerciseCategory.values().find { it.name.lowercase() == item.data.category.lowercase() }
                            ?.name ?: WgerExerciseCategory.Unknown.name,
                        "https://wger.de${item.data.image}",
                        "https://wger.de${item.data.imageThumbnail}",
//                        muscles = exerciseData.firstOrNull()?.muscles.orEmpty() +
//                                exerciseData.firstOrNull()?.musclesSecondary.orEmpty()
                    ))
//                }
                }
            } else {
                val exerciseResult = service.getExercises(
                    name = query,
                    muscle = WgerMuscle.values().find { it.coloquial.contains(muscleFilter.capitalizeWords()) },
                    category = WgerExerciseCategory.values().find { it.name.lowercase() == category.lowercase() },
                    limit = PAGE_SIZE,
                    offset = page * state.config.pageSize
                )

                exerciseResult.results.forEach { item ->
//                    val exerciseData = service.getExerciseImages(
//                        id = item.id, limit = 1, offset = 0).results
//                    exerciseData.firstOrNull()?.let { exercise ->
                        searchedExercises.add(
                        WgerSearchData(
                            item.id,
                            item.exerciseBase,
                            item.name,
                            WgerExerciseCategory.values().find { it.number == item.category }
                                ?.name ?: WgerExerciseCategory.Unknown.name,
                            null, null,
//                            exerciseData.firstOrNull()?.image,
//                            exerciseData.firstOrNull()?.image,
                            muscles = item.muscles + item.musclesSecondary
                        )
                    ) }
//                }

                nextPage = exerciseResult.next
            }

            Timber.d("*** results for page $page = " + searchedExercises.joinToString(", ") { it.name })
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys()
//                    database.exerciseDao().clearRemoteExercises()
                }
                val prevKey = if (page == STARTING_PAGE) null else page - 1
                val nextKey = if (nextPage == null) null else page + 1
                val keys = searchedExercises.map { wgerExercise ->
                    RemoteKeys(exerciseName = wgerExercise.name, prevKey = prevKey, nextKey = nextKey)
                }

                database.remoteKeysDao().insertAll(keys)
                database.exerciseDao().insertAll(searchedExercises.map { wgerExercise ->
                    DbExercise(
                        eId = 0L,
                        name = wgerExercise.name,
                        musclesWorked = wgerExercise.muscles?.flatMap { muscle ->
                            WgerMuscle.values().find { it.number == muscle }?.coloquial.orEmpty()
                        }.orEmpty(),
                        userCreated = false,
                        category = wgerExercise.category,
                        thumbnailUrl = wgerExercise.image
                    )
                })
            }
            return MediatorResult.Success(endOfPaginationReached = nextPage == null)
        } catch (exception: IOException) {
            Timber.e(exception)
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Timber.e(exception)
            return MediatorResult.Error(exception)
        }

    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ExerciseWithSets>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { ex ->
                // Get the remote keys of the first items retrieved
                database.remoteKeysDao().remoteKeysExerciseName(ex.exercise.name)
            }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ExerciseWithSets>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { ex ->
                // Get the remote keys of the last item retrieved
                database.remoteKeysDao().remoteKeysExerciseName(exerciseName = ex.exercise.name)
            }
    }


    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, ExerciseWithSets>
    ): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.exercise?.name?.let { name ->
                database.remoteKeysDao().remoteKeysExerciseName(name)
            }
        }
    }
}
