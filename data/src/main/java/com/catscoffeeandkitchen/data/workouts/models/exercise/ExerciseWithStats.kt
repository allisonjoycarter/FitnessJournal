package com.catscoffeeandkitchen.data.workouts.models.exercise

import androidx.room.Embedded
import java.time.OffsetDateTime

data class ExerciseWithStats(
    val exerciseName: String,
    val lastCompletedAt: OffsetDateTime? = null,
    val amountPerformed: Int? = null,
    val amountCompletedThisWeek: Int? = null,
    val highestWeightInKilograms: Float? = null,
    val highestWeightInPounds: Float? = null,
)
