package com.catscoffeeandkitchen.domain.usecases.workoutplan

import com.catscoffeeandkitchen.domain.interfaces.WorkoutPlanRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
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

class GetWorkoutPlanByAddedDateUseCase @Inject constructor(
    val repository: WorkoutPlanRepository
) {
    fun run(addedAt: OffsetDateTime): Flow<DataState<WorkoutPlan>> = flow {
        emit(DataState.Loading())
        val workout = repository.getWorkoutPlanByAddedDate(addedAt)
        emit(DataState.Success(workout))
    }
        .catch { error ->
            Timber.e(error)
            emit(DataState.Error(error))
        }
        .flowOn(Dispatchers.IO)
}