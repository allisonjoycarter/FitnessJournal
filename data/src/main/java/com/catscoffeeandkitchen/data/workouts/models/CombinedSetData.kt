package com.catscoffeeandkitchen.data.workouts.models

import com.catscoffeeandkitchen.domain.models.ExerciseSetType

data class CombinedSetData(
    val exerciseId: Long,
    val workoutId: Long,
    val sId: Long,
    val name: String,
    val musclesWorked: List<String>,
    val reps: Int,
    val weightInPounds: Int,
    val weightInKilograms: Int,
    val repsInReserve: Int,
    val perceivedExertion: Int,
    val setNumberInWorkout: Int,
    val isComplete: Boolean,
    val type: ExerciseSetType
)
