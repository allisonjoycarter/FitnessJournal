package com.catscoffeeandkitchen.domain.models

import java.time.OffsetDateTime

data class WorkoutPlan(
    val addedAt: OffsetDateTime,
    val name: String = "New Workout Plan",
    val note: String? = null,
    val exercises: List<ExpectedSet> = emptyList(),
)
