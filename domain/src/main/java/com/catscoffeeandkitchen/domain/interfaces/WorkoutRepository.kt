package com.catscoffeeandkitchen.domain.interfaces

import androidx.paging.PagingData
import com.catscoffeeandkitchen.domain.models.*
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

interface WorkoutRepository {

    suspend fun getWorkouts(): List<Workout>

    fun getPagedWorkouts(): Flow<PagingData<Workout>>

    suspend fun getCompletedWorkouts(): List<Workout>

    suspend fun getWorkoutCompletedDates(monthsBack: Int): List<OffsetDateTime>

    suspend fun getWorkoutByAddedDate(addedAt: OffsetDateTime): Workout

    suspend fun createWorkout(workout: Workout, planAddedAt: OffsetDateTime?): Workout

    suspend fun updateWorkout(workout: Workout): Workout

    suspend fun deleteWorkout(workout: Workout)

    suspend fun getExercises(names: List<String>? = null): List<Exercise>

    suspend fun getExerciseByName(name: String): Exercise?

    fun getPagedExercises(search: String?, muscle: String?, category: String?): Flow<PagingData<Exercise>>

    suspend fun createExercise(exercise: Exercise): Exercise
    suspend fun addEntry(workoutEntry: WorkoutEntry, workoutAddedAt: OffsetDateTime): WorkoutEntry

    suspend fun updateExercise(exercise: Exercise, workout: Workout? = null)

    suspend fun removeEntryFromWorkout(entry: WorkoutEntry, workoutAddedAt: OffsetDateTime)

    suspend fun updateCompletedSet(workout: Workout, exerciseSet: ExerciseSet)

    suspend fun deleteSet(setId: Long)

    suspend fun swapEntryPosition(
        workoutAddedAt: OffsetDateTime,
        entry: WorkoutEntry,
        newPosition: Int
    )

    suspend fun replaceGroupWithExercise(
        workoutAddedAt: OffsetDateTime,
        group: ExerciseGroup,
        exercise: Exercise,
        position: Int,
        expectedSet: ExpectedSet?
    )

    suspend fun replaceExerciseWithGroup(
        workoutAddedAt: OffsetDateTime,
        entry: WorkoutEntry,
    ): WorkoutEntry
}