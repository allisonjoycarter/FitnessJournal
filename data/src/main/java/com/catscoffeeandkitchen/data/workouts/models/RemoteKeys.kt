package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteKeys(
    @PrimaryKey val exerciseName: String,
    val prevKey: Int?,
    val nextKey: Int?
)
