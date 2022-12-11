package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExerciseGroupEntity(
    @PrimaryKey(autoGenerate = true) val gId: Long,
    val name: String? = null,
)
