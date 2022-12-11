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

class GetExercisesUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    fun run(names: List<String>? = null): Flow<DataState<List<Exercise>>> = flow {
        emit(DataState.Loading())
        val exercises = workoutRepository.getExercises(names)
        emit(DataState.Success(exercises))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

