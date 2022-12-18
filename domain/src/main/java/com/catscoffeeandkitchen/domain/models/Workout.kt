package com.catscoffeeandkitchen.domain.models

import java.time.OffsetDateTime

data class Workout(
    val addedAt: OffsetDateTime,
    val name: String = "New Workout",
    val note: String? = null,
    val completedAt: OffsetDateTime? = null,
    val plan: WorkoutPlan? = null,
    val sets: List<ExerciseSet> = emptyList(),
    val entries: List<WorkoutEntry> = emptyList(),
)
