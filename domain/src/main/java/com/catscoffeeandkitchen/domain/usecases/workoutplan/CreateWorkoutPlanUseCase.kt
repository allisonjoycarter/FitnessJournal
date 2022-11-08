package com.catscoffeeandkitchen.domain.usecases.workoutplan

import com.catscoffeeandkitchen.domain.interfaces.WorkoutPlanRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class CreateWorkoutPlanUseCase @Inject constructor(
    val repository: WorkoutPlanRepository
) {
    fun run(workout: WorkoutPlan): Flow<DataState<WorkoutPlan>> = flow {
        emit(DataState.Loading())
        repository.createWorkoutPlan(workout)
        emit(DataState.Success(workout))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}