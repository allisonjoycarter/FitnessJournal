package com.catscoffeeandkitchen.domain.models

import java.time.OffsetDateTime

data class ExerciseSet(
    val id: Long,
    val reps: Int,
    val exercise: Exercise,
    val setNumberInWorkout: Int = 0,
    val weightInPounds: Int = 0,
    val weightInKilograms: Int = 0,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val isComplete: Boolean = false,
    val completedAt: OffsetDateTime? = null,
    val type: ExerciseSetType = ExerciseSetType.Working
)
