package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout

interface ExerciseSetRepository {
    suspend fun updateExerciseSet(exerciseSet: ExerciseSet)

    suspend fun addExerciseSet(
        workout: Workout,
        exercise: Exercise,
        exerciseSet: ExerciseSet
    )

    suspend fun addExerciseSets(
        workout: Workout,
        exercise: Exercise,
        exerciseSets: List<ExerciseSet>
    )

    suspend fun getCompletedSetsForExercise(name: String): List<ExerciseSet>
}