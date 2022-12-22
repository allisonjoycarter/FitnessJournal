package com.catscoffeeandkitchen.domain.interfaces

import androidx.paging.PagingData
import com.catscoffeeandkitchen.domain.models.*
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

interface WorkoutRepository {

    suspend fun getWorkouts(): List<Workout>

    fun getPagedWorkouts(): Flow<PagingData<Workout>>

    suspend fun getWorkoutCompletedDates(monthsBack: Int): List<OffsetDateTime>

    suspend fun getWorkout(id: Long): Workout

    suspend fun createWorkout(workout: Workout, planId: Long?): Workout

    suspend fun updateWorkout(workout: Workout): Workout

    suspend fun deleteWorkout(workout: Workout)

    suspend fun addEntry(workoutEntry: WorkoutEntry, workoutAddedAt: OffsetDateTime): WorkoutEntry

    suspend fun removeEntryFromWorkout(entry: WorkoutEntry, workoutAddedAt: OffsetDateTime)

    suspend fun updateCompletedSet(workout: Workout, exerciseSet: ExerciseSet)

    suspend fun deleteSet(set: ExerciseSet, workoutId: Long)

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