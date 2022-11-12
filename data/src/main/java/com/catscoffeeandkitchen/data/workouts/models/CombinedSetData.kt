package com.catscoffeeandkitchen.data.workouts.models

import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import java.time.OffsetDateTime

data class CombinedSetData(
    val exerciseId: Long,
    val workoutId: Long,
    val sId: Long,

    val name: String,
    val musclesWorked: List<String>,
    val category: String? = null,
    val thumbnailUrl: String? = null,
    val equipmentType: ExerciseEquipmentType = ExerciseEquipmentType.Bodyweight,

    val reps: Int,
    val weightInPounds: Float,
    val weightInKilograms: Float,
    val repsInReserve: Int,
    val perceivedExertion: Int,
    val setNumberInWorkout: Int,
    val completedAt: OffsetDateTime?,
    val type: ExerciseSetType
)
