package com.catscoffeeandkitchen.domain.usecases.exercise

import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.WorkoutEntry
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

class RemoveExerciseFromWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    fun run(entry: WorkoutEntry, workoutAddedAt: OffsetDateTime): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading())
        repository.removeEntryFromWorkout(entry, workoutAddedAt = workoutAddedAt)
        emit(DataState.Success(true))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}
