package com.catscoffeeandkitchen.domain.usecases.exercise

import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
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

class ReplaceExerciseWithGroupUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    fun run(
        workoutAddedAt: OffsetDateTime,
        entry: WorkoutEntry,
    ): Flow<DataState<WorkoutEntry>> = flow {
        emit(DataState.Loading())
        val result = repository.replaceExerciseWithGroup(workoutAddedAt, entry)
        emit(DataState.Success(result))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}