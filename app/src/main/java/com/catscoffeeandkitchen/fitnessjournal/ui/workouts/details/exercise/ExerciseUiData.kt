package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import java.time.OffsetDateTime

data class ExerciseUiData(
    val workoutId: Long,
    val entry: WorkoutEntry,
    val unit: WeightUnit,
    val isFirstExercise: Boolean = false,
    val isLastExercise: Boolean = false,
    val useKeyboard: Boolean = false,
    val wasChosenFromGroup: Boolean = false,
)