package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val wpId: Long,
    val addedAt: OffsetDateTime = OffsetDateTime.now(),
    val name: String = "New Workout",
    val note: String? = null,
)
