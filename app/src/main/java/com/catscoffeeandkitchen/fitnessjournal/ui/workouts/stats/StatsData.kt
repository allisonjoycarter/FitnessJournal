package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats

import java.time.OffsetDateTime

data class StatsData(
    val date: OffsetDateTime,
    val repMax: Float,
    val totalVolume: Float,
    val highestWeight: Float,
    val reps: Float,
)
