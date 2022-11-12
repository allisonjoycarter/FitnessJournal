package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.Workout

interface ExerciseSetRepository {
    suspend fun updateExerciseSet(exerciseSet: ExerciseSet)

    suspend fun addExerciseSetWithPopulatedData(
        workout: Workout,
        exercise: Exercise,
        exerciseSet: ExerciseSet,
        expectedSet: ExpectedSet?
    )

    suspend fun addExerciseSets(
        workout: Workout,
        exercise: Exercise,
        exerciseSets: List<ExerciseSet>
    )

    suspend fun getCompletedSetsForExercise(name: String): List<ExerciseSet>

    suspend fun changeExerciseForSets(setIds: List<Long>, exercise: Exercise)
}