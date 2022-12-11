package com.catscoffeeandkitchen.domain.usecases

import androidx.paging.PagingData
import androidx.paging.PagingSource
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

class GetPagedExercisesUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    fun run(search: String? = null, muscle: String? = null, category: String? = null): Flow<PagingData<Exercise>> =
        workoutRepository.getPagedExercises(search = search, muscle = muscle, category = category)
}

