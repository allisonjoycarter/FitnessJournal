package com.catscoffeeandkitchen.domain.models

import java.time.DayOfWeek
import java.time.OffsetDateTime

data class WorkoutWeekStats(
    val dates: List<OffsetDateTime>,
    val averageWorkoutsPerWeek: Double,
    val mostCommonDays: List<DayOfWeek>,
    val mostCommonTimes: List<Int>,
)
