package com.catscoffeeandkitchen.data.workouts.models.exercise

import androidx.room.Embedded

data class ExerciseWithPositionCount(
    @Embedded val exercise: ExerciseEntity,
    val amountPerformed: Int,
)
