package com.catscoffeeandkitchen.domain.usecases.exerciseset

import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject


class ReplaceExerciseForSetsUseCase @Inject constructor(
    private val repository: ExerciseSetRepository
) {
    fun run(
        setIds: List<Long>,
        exercise: Exercise,
        exercisePosition: Int,
        workoutAddedAt: OffsetDateTime
    ): Flow<DataState<Exercise>> = flow {
        emit(DataState.Loading())
        val result = repository.changeExerciseForSets(setIds, exercise, exercisePosition, workoutAddedAt)
        emit(DataState.Success(result))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

