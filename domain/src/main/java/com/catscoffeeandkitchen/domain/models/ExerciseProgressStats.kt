package com.catscoffeeandkitchen.domain.models

import java.time.Duration

data class ExerciseProgressStats(
    val exercise: Exercise,
    val worstSet: ExerciseSet,
    val bestSet: ExerciseSet,
    val amountOfTime: Duration,
    val starting1RM: Float,
    val ending1RM: Float
)
