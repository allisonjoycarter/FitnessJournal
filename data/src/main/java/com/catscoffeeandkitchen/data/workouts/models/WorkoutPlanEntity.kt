package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity
data class WorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true) val wpId: Long,
    val addedAt: OffsetDateTime = OffsetDateTime.now(),
    val name: String = "New Workout",
    val note: String? = null,
    @ColumnInfo(defaultValue = "") val daysOfWeek: List<String> = emptyList(),
)
