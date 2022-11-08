package com.catscoffeeandkitchen.domain.usecases

import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class CreateExercisesUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    fun run(exercise: Exercise): Flow<DataState<Exercise>> = flow {
        emit(DataState.Loading())
        val ex = workoutRepository.createExercise(exercise)
        emit(DataState.Success(ex))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

