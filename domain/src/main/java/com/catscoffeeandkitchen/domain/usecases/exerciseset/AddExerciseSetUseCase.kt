package com.catscoffeeandkitchen.domain.usecases.exerciseset

import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject


class AddExerciseSetUseCase @Inject constructor(
    private val repository: ExerciseSetRepository
) {
    fun run(
        entry: WorkoutEntry,
        set: ExerciseSet,
        workoutId: Long,
    ): Flow<DataState<WorkoutEntry>> = flow {
        emit(DataState.Loading())
        val result = repository.addExerciseSetWithPopulatedData(
            entry = entry,
            exerciseSet = set,
            workoutId = workoutId,
        )
        emit(DataState.Success(result))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

