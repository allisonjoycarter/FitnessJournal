package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.*
import java.time.OffsetDateTime

interface ExerciseSetRepository {
    suspend fun updateExerciseSet(exerciseSet: ExerciseSet): ExerciseSet
    suspend fun updateMultipleSets(sets: List<ExerciseSet>)

    suspend fun addExerciseSetWithPopulatedData(
        entry: WorkoutEntry,
        exerciseSet: ExerciseSet,
        workoutAddedAt: OffsetDateTime,
    ): WorkoutEntry

    suspend fun addExerciseSets(
        workoutAddedAt: OffsetDateTime,
        entry: WorkoutEntry,
        exerciseSets: List<ExerciseSet>
    ): WorkoutEntry

    suspend fun getCompletedSetsForExercise(name: String): List<ExerciseSet>

    suspend fun changeExerciseForSets(
        setIds: List<Long>,
        exercise: Exercise,
        position: Int,
        workoutAddedAt: OffsetDateTime
    ): Exercise
}