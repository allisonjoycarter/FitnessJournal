package com.catscoffeeandkitchen.domain.usecases.exercise

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.catscoffeeandkitchen.domain.interfaces.ExerciseRepository
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
    private val repository: ExerciseRepository
) {
    fun run(search: String? = null, muscle: String? = null, category: String? = null): Flow<PagingData<Exercise>> =
        repository.getPagedExercises(search = search, muscle = muscle, category = category)
}

