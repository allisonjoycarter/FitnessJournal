package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import java.time.OffsetDateTime

data class ExerciseUiData(
    val workoutAddedAt: OffsetDateTime,
    val exercise: Exercise,
    val sets: List<ExerciseSet>,
    val expectedSet: ExpectedSet?,
    val unit: WeightUnit,
    val isFirstExercise: Boolean = false,
    val isLastExercise: Boolean = false,
    val useKeyboard: Boolean = false,
    val wasChosenFromGroup: Boolean = false,
)
