package com.catscoffeeandkitchen.domain.models

import java.time.OffsetDateTime

data class WorkoutPlan(
    val id: Long,
    val addedAt: OffsetDateTime = OffsetDateTime.now(),
    val name: String = "New Workout Plan",
    val note: String? = null,
    val entries: List<ExpectedSet> = emptyList(),
)
