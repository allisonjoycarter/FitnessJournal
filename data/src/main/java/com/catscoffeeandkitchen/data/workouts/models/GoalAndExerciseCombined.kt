package com.catscoffeeandkitchen.data.workouts.models

data class GoalAndExerciseCombined(
    val workoutPlanId: Long,
    val sets: Int,
    val setNumberInWorkout: Int = 0,
    val reps: Int,
    val repRangeMax: Int = 0,
    val repRangeMin: Int = 0,
    val weightInPounds: Int = 0,
    val weightInKilograms: Int = 0,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val note: String = "",

    val name: String,
    val musclesWorked: List<String> = emptyList()
)
