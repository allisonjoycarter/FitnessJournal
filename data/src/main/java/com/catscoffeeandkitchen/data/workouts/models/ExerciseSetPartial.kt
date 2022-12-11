package com.catscoffeeandkitchen.data.workouts.models

import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import java.time.OffsetDateTime

data class ExerciseSetPartial(
    val sId: Long,
    val reps: Int = 1,
    val weightInPounds: Float = 0f,
    val weightInKilograms: Float = 0f,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val setNumber: Int = 1,
    val completedAt: OffsetDateTime? = null,
    val type: ExerciseSetType = ExerciseSetType.Working
)
