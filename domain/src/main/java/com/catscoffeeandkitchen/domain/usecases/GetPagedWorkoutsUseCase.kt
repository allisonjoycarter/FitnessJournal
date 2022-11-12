package com.catscoffeeandkitchen.domain.usecases

import androidx.paging.PagingData
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class GetPagedWorkoutsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    fun run(): Flow<PagingData<Workout>> = workoutRepository.getPagedWorkouts()
}

