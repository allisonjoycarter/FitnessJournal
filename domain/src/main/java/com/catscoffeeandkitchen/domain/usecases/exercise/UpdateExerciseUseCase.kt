package com.catscoffeeandkitchen.domain.usecases.exercise

import com.catscoffeeandkitchen.domain.interfaces.ExerciseRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject


class UpdateExerciseUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    fun run(exercise: Exercise, workout: Workout? = null): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading())
        val ex = repository.updateExercise(exercise, workout)
        emit(DataState.Success(true))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

