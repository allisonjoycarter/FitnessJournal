package com.catscoffeeandkitchen.domain.models

data class Exercise(
    val name: String,
    val musclesWorked: List<String>,
    val category: String? = null,
    val thumbnailUrl: String? = null,
    val equipmentType: ExerciseEquipmentType = ExerciseEquipmentType.Bodyweight,
)
