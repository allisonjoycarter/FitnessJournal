package com.catscoffeeandkitchen.domain.usecases

import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class GetWorkoutByAddedDateUseCase @Inject constructor(
    val workoutRepository: WorkoutRepository
) {
    fun run(addedAt: OffsetDateTime): Flow<DataState<Workout>> = flow {
        emit(DataState.Loading())
        val workout = workoutRepository.getWorkoutByAddedDate(addedAt)
        emit(DataState.Success(workout))
    }
        .catch { error ->
            Timber.e(error)
            emit(DataState.Error(error))
        }
        .flowOn(Dispatchers.IO)
}