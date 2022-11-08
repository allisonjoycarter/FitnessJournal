package com.catscoffeeandkitchen.domain.usecases.exerciseset

import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject


class AddExerciseSetUseCase @Inject constructor(
    private val repository: ExerciseSetRepository
) {
    fun run(set: ExerciseSet, exercise: Exercise, workout: Workout): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading())
        Timber.d("*** running use case, set number = ${set.setNumberInWorkout}")
        repository.addExerciseSet(exerciseSet = set, exercise = exercise, workout = workout)
        emit(DataState.Success(true))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

