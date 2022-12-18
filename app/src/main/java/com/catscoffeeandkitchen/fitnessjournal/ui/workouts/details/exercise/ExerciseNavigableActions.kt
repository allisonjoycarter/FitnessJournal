package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup

interface ExerciseNavigableActions {
    fun addExercise()
    fun addExerciseGroup()
    fun swapExerciseAt(position: Int)
    fun editGroup(group: ExerciseGroup)
}