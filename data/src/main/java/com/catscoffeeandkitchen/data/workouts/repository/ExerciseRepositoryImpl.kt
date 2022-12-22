package com.catscoffeeandkitchen.data.workouts.repository

import androidx.paging.*
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.GroupExerciseXRef
import com.catscoffeeandkitchen.domain.interfaces.ExerciseRepository
import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity
import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.OffsetDateTime
import javax.inject.Inject


class ExerciseRepositoryImpl @Inject constructor(
    private val database: FitnessJournalDb,
    private val exerciseSearchService: ExerciseSearchService
): ExerciseRepository {

    override suspend fun getExercises(names: List<String>?): List<Exercise> {
        if (names != null) {
            return database.exerciseDao().getExercisesByName(names).map { exercise ->
                exercise.toExercise()
            }
        }
        return database.exerciseDao().getAllExercises().map { exerciseWithCount ->
            exerciseWithCount.exercise.toExercise(amountOfSets = exerciseWithCount.amountPerformed)
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getPagedExercises(search: String?, muscle: String?, category: String?): Flow<PagingData<Exercise>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = ExerciseRemoteMediator(
                search.orEmpty(),
                muscle.orEmpty(),
                category.orEmpty(),
                exerciseSearchService,
                database
            ),
            pagingSourceFactory = {
                if (search.isNullOrEmpty() && muscle.isNullOrEmpty() && category.isNullOrEmpty()) {
                    database.exerciseDao().getAllPagedExercises()
                } else {
                    database.exerciseDao().getPagedExercisesByName(
                        search.orEmpty(),
                        muscle.orEmpty(),
                        category.orEmpty()
                    )
                }
            }
        ).flow.map { pagingData ->
            pagingData.map { it.exercise.toExercise() }
        }
    }

    override suspend fun getExerciseByName(name: String): Exercise? {
        val dbExercise = database.exerciseDao().getExerciseByName(name) ?: return null
        return dbExercise.toExercise()
    }

    override suspend fun createExercise(exercise: Exercise): Exercise {
        database.exerciseDao().insert(exercise = exercise.toDbExercise(id = 0L, userCreated = true))
        return exercise
    }

    override suspend fun updateExercise(exercise: Exercise, workout: Workout?) {
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)
        dbExercise?.let { dbEx ->
            database.exerciseDao().update(exercise.toDbExercise(id = dbEx.eId))
        }
    }

    override suspend fun createGroup(groupName: String, exerciseNames: List<String>): ExerciseGroup {
        val exercises = database.exerciseDao().getExercisesByName(exerciseNames)
        val groupId = database.exerciseGroupDao().insert(
            ExerciseGroupEntity(
                gId = 0L,
                name = groupName,
            )
        )

        database.exerciseGroupDao().insertAllRefs(exercises.map { ex ->
            GroupExerciseXRef(
                egxrId = 0L,
                groupId = groupId,
                exerciseId = ex.eId
            )
        })

        return ExerciseGroup(
            id = groupId,
            name = groupName,
            exercises = exercises.map { it.toExercise() }
        )
    }

    override suspend fun updateGroup(group: ExerciseGroup) {
        database.exerciseGroupDao().update(group.toEntity())
    }

    override suspend fun updateGroupExercises(
        group: ExerciseGroup,
        exerciseNames: List<String>
    ): ExerciseGroup {
        database.exerciseGroupDao().removeGroupXRefs(group.id)

        val exercises = database.exerciseDao().getExercisesByName(exerciseNames)
        database.exerciseGroupDao().insertAllRefs(exercises.map { ex ->
            GroupExerciseXRef(
                egxrId = 0L,
                groupId = group.id,
                exerciseId = ex.eId
            )
        })

        return group.copy(exercises = exercises.map { exercise ->
            val stats = database.exerciseDao().getExerciseWithStatsByName(
                exercise.name,
                startOfWeek = OffsetDateTime.now().inUTC().with(DayOfWeek.MONDAY).withHour(0)
                    .toUTCEpochMilli(),
                currentTime = OffsetDateTime.now().toUTCEpochMilli()
            )
            exercise.toExercise(
                stats = stats?.toStats()
            )
        })
    }

    override suspend fun removeGroup(group: ExerciseGroup) {
        database.exerciseGroupDao().delete(group.toEntity())
    }

    override suspend fun getGroups(): List<ExerciseGroup> {
        return database.exerciseGroupDao().getGroups().map { entry ->
            entry.group.toExerciseGroup(
                exercises = entry.exercises.map { exercise ->
                    val stats = database.exerciseDao().getExerciseWithStatsByName(
                        exercise.name,
                        startOfWeek = OffsetDateTime.now().inUTC().with(DayOfWeek.MONDAY).withHour(0).toUTCEpochMilli(),
                        currentTime = OffsetDateTime.now().toUTCEpochMilli()
                    )
                    exercise.toExercise(
                        stats = stats?.toStats()
                    )
                }
            )
        }
    }
}