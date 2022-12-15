package com.catscoffeeandkitchen.domain.models

import java.time.OffsetDateTime

data class ExerciseSet(
    val id: Long,
    val chosenFromGroup: Long? = null,
    val reps: Int,
    val exercise: Exercise,
    val setNumber: Int = 1,
    val weightInPounds: Float = 0f,
    val weightInKilograms: Float = 0f,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val isComplete: Boolean = false,
    val completedAt: OffsetDateTime? = null,
    val type: ExerciseSetType = ExerciseSetType.Working,
    val seconds: Int = 0,
    val modifier: ExerciseSetModifier? = null,
)
