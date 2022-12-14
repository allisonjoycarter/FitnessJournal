package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import java.time.OffsetDateTime

interface ExerciseNavigableActions {
    fun addExercise()
    fun addExerciseGroup()
    fun swapExercise(exercise: Exercise)
    fun editGroup(group: ExerciseGroup)
}