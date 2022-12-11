package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.Workout
import java.time.OffsetDateTime

interface ExerciseSetRepository {
    suspend fun updateExerciseSet(exerciseSet: ExerciseSet): ExerciseSet

    suspend fun addExerciseSetWithPopulatedData(
        workoutAddedAt: OffsetDateTime,
        exerciseName: String,
        exerciseSet: ExerciseSet,
        expectedSet: ExpectedSet?
    ): ExerciseSet

    suspend fun addExerciseSets(
        workoutAddedAt: OffsetDateTime,
        exercise: Exercise,
        exerciseSets: List<ExerciseSet>
    )

    suspend fun getCompletedSetsForExercise(name: String): List<ExerciseSet>

    suspend fun changeExerciseForSets(
        setIds: List<Long>,
        exercise: Exercise,
        position: Int,
        workoutAddedAt: OffsetDateTime
    ): Exercise
}