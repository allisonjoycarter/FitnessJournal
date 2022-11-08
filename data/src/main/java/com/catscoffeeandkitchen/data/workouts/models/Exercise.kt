package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [Index(value = ["name"], unique = true)]
)
data class Exercise(
    @PrimaryKey(autoGenerate = true) val eId: Long,
    val name: String,
    val musclesWorked: List<String> = emptyList(),
    val userCreated: Boolean = true,
    val category: String? = null,
    val thumbnailUrl: String? = null
)
