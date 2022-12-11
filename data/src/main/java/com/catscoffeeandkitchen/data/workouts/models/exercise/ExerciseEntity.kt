package com.catscoffeeandkitchen.data.workouts.models.exercise

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType

@Entity(
    indices = [Index(value = ["name"], unique = true)]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val eId: Long,
    val name: String,
    val musclesWorked: List<String> = emptyList(),
    val userCreated: Boolean = true,
    val category: String? = null,
    val thumbnailUrl: String? = null,
    val equipmentType: ExerciseEquipmentType = ExerciseEquipmentType.Bodyweight,
)
