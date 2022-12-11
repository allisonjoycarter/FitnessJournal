package com.catscoffeeandkitchen.domain.usecases.exercisegroup

import com.catscoffeeandkitchen.domain.interfaces.ExerciseRepository
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

class RemoveExerciseGroupUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    fun run(
        exerciseGroup: ExerciseGroup
    ): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading())
        repository.removeGroup(exerciseGroup)
        emit(DataState.Success(true))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
    }