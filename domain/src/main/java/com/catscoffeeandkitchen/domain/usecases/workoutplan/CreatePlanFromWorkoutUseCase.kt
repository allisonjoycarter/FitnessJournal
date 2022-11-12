package com.catscoffeeandkitchen.domain.usecases.workoutplan

import com.catscoffeeandkitchen.domain.interfaces.WorkoutPlanRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

class CreatePlanFromWorkoutUseCase @Inject constructor(
    val repository: WorkoutPlanRepository
) {
    fun run(workout: Workout): Flow<DataState<OffsetDateTime>> = flow {
        emit(DataState.Loading())
        val addedAt = repository.createPlanFromWorkout(workout)
        emit(DataState.Success(addedAt))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}