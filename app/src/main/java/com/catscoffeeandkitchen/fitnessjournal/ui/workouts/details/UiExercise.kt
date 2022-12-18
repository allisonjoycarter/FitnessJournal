package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet

data class UiExercise(
    val name: String,
    val exercise: Exercise? = null,
    val group: ExerciseGroup? = null,
    val position: Int = 1,
)