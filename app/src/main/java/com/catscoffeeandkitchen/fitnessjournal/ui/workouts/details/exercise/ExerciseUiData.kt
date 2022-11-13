package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit

data class ExerciseUiData(
    val exercise: Exercise,
    val sets: List<ExerciseSet>,
    val expectedSet: ExpectedSet?,
    val unit: WeightUnit,
)
