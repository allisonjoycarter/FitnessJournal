package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import kotlinx.coroutines.Job
import java.time.OffsetDateTime

interface ExerciseUiActions {
    fun addExercise(name: String): Job
    fun swapExercise(exercisePosition: Int, exercise: Exercise): Job
    fun removeExercise(exercise: Exercise): Job
    fun moveExerciseTo(exercise: Exercise, newPosition: Int): Job

    fun removeSet(setId: Long): Job
    fun updateSet(set: ExerciseSet): Job
    fun updateSets(sets: List<ExerciseSet>): Job
    fun addExerciseSet(exerciseName: String, workoutAddedAt: OffsetDateTime): Job
    fun addWarmupSets(workoutAddedAt: OffsetDateTime, exercise: Exercise,
                        sets: List<ExerciseSet>, unit: WeightUnit): Job

    fun replaceWithGroup(exercisePosition: Int, exercise: Exercise): Job
    fun selectExerciseFromGroup(
        group: ExerciseGroup,
        exercise: Exercise,
        position: Int,
        expectedSet: ExpectedSet? = null
    ): Job
}