package com.catscoffeeandkitchen.domain.interfaces

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.catscoffeeandkitchen.domain.models.CompletedExerciseSet
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

interface WorkoutRepository {

    suspend fun getWorkouts(): List<Workout>

    suspend fun getCompletedWorkouts(): List<Workout>

    suspend fun getWorkoutByAddedDate(addedAt: OffsetDateTime): Workout

    suspend fun createWorkout(workout: Workout, planAddedAt: OffsetDateTime?): Workout

    suspend fun updateLatestWorkout(workout: Workout): Workout

    suspend fun updateWorkout(workout: Workout): Workout

    suspend fun deleteWorkout(workout: Workout)

    suspend fun getExercises(): List<Exercise>

    suspend fun getExerciseByName(name: String): Exercise?

    fun getPagedExercises(search: String?, muscle: String?, category: String?): Flow<PagingData<Exercise>>

    suspend fun createExercise(exercise: Exercise): Exercise

    suspend fun updateExercise(exercise: Exercise, workout: Workout? = null)

    suspend fun removeExerciseFromWorkout(exercise: Exercise, workout: Workout)

    suspend fun updateCompletedSet(workout: Workout, exerciseSet: ExerciseSet)

    suspend fun deleteSet(setId: Long)
}