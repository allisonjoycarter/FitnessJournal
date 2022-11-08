package com.catscoffeeandkitchen.domain.usecases

import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class GetSetsAndExercisesUseCase @Inject constructor(
    private val exerciseSetRepository: ExerciseSetRepository
) {
    fun run(exerciseName: String?): Flow<DataState<List<ExerciseSet>>> = flow {
        emit(DataState.Loading())
        if (exerciseName != null) {
            val exercises = exerciseSetRepository.getCompletedSetsForExercise(exerciseName)
            emit(DataState.Success(exercises))
        } else {
            emit(DataState.NotSent())
        }
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

