package com.catscoffeeandkitchen.domain.usecases.home

import com.catscoffeeandkitchen.domain.interfaces.HomeRepository
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

class GetNextWorkoutPlanUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    fun run(): Flow<DataState<WorkoutPlan?>> = flow {
        emit(DataState.Loading())
        Timber.d("getting workouts")
        val ex = repository.getNextWorkoutPlan()
        emit(DataState.Success(ex))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

