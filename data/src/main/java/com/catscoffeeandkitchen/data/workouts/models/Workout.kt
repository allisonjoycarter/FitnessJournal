package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            childColumns = ["planId"],
            parentColumns = ["wpId"],
            entity = WorkoutPlan::class,
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("planId"),
    ]
)
data class Workout(
    @PrimaryKey(autoGenerate = true) val wId: Long,
    val planId: Long? = null,
    val minutesToComplete: Int = 0,
    val addedAt: OffsetDateTime = OffsetDateTime.now(),
    val completedAt: OffsetDateTime? = null,
    val name: String = "New Workout",
    val note: String? = null,
)
