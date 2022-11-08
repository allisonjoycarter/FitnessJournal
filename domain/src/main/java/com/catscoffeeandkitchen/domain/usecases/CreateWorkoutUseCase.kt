package com.catscoffeeandkitchen.domain.usecases

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

class CreateWorkoutUseCase @Inject constructor(
    val workoutRepository: WorkoutRepository
) {
    fun run(workout: Workout, planAddedAt: OffsetDateTime? = null): Flow<DataState<Workout>> = flow {
        emit(DataState.Loading())
        workoutRepository.createWorkout(workout, planAddedAt)
        emit(DataState.Success(workout))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}