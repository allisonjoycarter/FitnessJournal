package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import kotlinx.coroutines.Job
import java.time.OffsetDateTime

interface ExerciseUiActions {
    fun addExercise(name: String): Job
    fun swapExercise(exercisePosition: Int, exercise: Exercise): Job
    fun removeEntry(entry: WorkoutEntry): Job
    fun moveEntryTo(entry: WorkoutEntry, newPosition: Int): Job
    fun removeSet(setId: Long): Job
    fun updateSet(set: ExerciseSet): Job
    fun updateSets(sets: List<ExerciseSet>): Job
    fun addExerciseSet(entry: WorkoutEntry, workoutAddedAt: OffsetDateTime): Job
    fun addWarmupSets(workoutAddedAt: OffsetDateTime, entry: WorkoutEntry, unit: WeightUnit): Job

    fun replaceWithGroup(entry: WorkoutEntry): Job
    fun selectExerciseFromGroup(
        group: ExerciseGroup,
        exercise: Exercise,
        position: Int,
        expectedSet: ExpectedSet? = null
    ): Job
}