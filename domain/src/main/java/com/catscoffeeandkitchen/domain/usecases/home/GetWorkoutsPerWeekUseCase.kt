package com.catscoffeeandkitchen.domain.usecases.home

import com.catscoffeeandkitchen.domain.interfaces.HomeRepository
import com.catscoffeeandkitchen.domain.models.WorkoutWeekStats
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class GetWorkoutsPerWeekUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    fun run(weeks: Int): Flow<DataState<WorkoutWeekStats>> = flow {
        emit(DataState.Loading())
        val ex = repository.getWorkoutWeekStats(weeks)
        emit(DataState.Success(ex))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

