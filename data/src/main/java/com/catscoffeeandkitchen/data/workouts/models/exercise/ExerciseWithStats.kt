package com.catscoffeeandkitchen.data.workouts.models.exercise

import androidx.room.Embedded
import java.time.OffsetDateTime

data class ExerciseWithStats(
    @Embedded val exercise: ExerciseEntity,
    val lastCompletedAt: OffsetDateTime? = null,
    val amountPerformed: Int? = null,
    val highestWeightInKilograms: Float? = null,
    val highestWeightInPounds: Float? = null,
)
