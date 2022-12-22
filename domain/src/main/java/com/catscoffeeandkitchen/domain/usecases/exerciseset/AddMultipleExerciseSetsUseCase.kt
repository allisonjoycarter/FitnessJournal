package com.catscoffeeandkitchen.domain.usecases.exerciseset

import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.models.WorkoutEntry
import com.catscoffeeandkitchen.domain.util.DataState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject


class AddMultipleExerciseSetsUseCase @Inject constructor(
    private val repository: ExerciseSetRepository
) {
    fun run(sets: List<ExerciseSet>, entry: WorkoutEntry, workoutId: Long): Flow<DataState<WorkoutEntry>> = flow {
        emit(DataState.Loading())
        val result = repository.addExerciseSets(exerciseSets = sets, entry = entry, workoutId = workoutId)
        emit(DataState.Success(result))
    }
        .catch { ex ->
            Timber.e(ex)
            emit(DataState.Error(ex))
        }
        .flowOn(Dispatchers.IO)
}

