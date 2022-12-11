package com.catscoffeeandkitchen.domain.usecases.workoutplan

import com.catscoffeeandkitchen.domain.interfaces.WorkoutPlanRepository
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class UpdateExpectedSetPositionUseCase @Inject constructor(
    private val repository: WorkoutPlanRepository
) {
    fun run(workout: WorkoutPlan, expectedSet: ExpectedSet, newSetNumber: Int): Flow<DataState<Boolean>> = flow {
        emit(DataState.Loading())
        repository.updateExpectedSetPosition(workout, expectedSet, newSetNumber)
        emit(DataState.Success(true))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}