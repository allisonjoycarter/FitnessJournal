package com.catscoffeeandkitchen.domain.usecases.home

import com.catscoffeeandkitchen.domain.interfaces.HomeRepository
import com.catscoffeeandkitchen.domain.models.ExerciseProgressStats
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

class GetMostImprovedExerciseUseCase @Inject constructor(
    private val repository: HomeRepository
) {
    fun run(weeksAgo: Int): Flow<DataState<ExerciseProgressStats?>> = flow {
        emit(DataState.Loading())
        val ex = repository.getMostImprovedExercise(weeksAgo)
        emit(DataState.Success(ex))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

