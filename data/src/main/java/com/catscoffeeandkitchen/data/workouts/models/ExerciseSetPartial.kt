package com.catscoffeeandkitchen.data.workouts.models

import com.catscoffeeandkitchen.domain.models.ExerciseSetType

data class ExerciseSetPartial(
    val sId: Long,
    val reps: Int = 1,
    val weightInPounds: Int = 0,
    val weightInKilograms: Int = 0,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val setNumberInWorkout: Int = 1,
    val isComplete: Boolean = false,
    val type: ExerciseSetType = ExerciseSetType.Working
)
