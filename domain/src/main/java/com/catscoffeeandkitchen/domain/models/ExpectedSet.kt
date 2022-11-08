package com.catscoffeeandkitchen.domain.models

data class ExpectedSet(
    val exercise: Exercise,
    val reps: Int = 0,
    val sets: Int = 0,
    val maxReps: Int = 0,
    val minReps: Int = 0,
    val perceivedExertion: Int = 0,
    val rir: Int = 0,
    val setNumberInWorkout: Int = 0,
    val note: String = "",
)
