package com.catscoffeeandkitchen.domain.models

data class ExerciseGroup(
    val id: Long,
    val name: String,
    val exercises: List<Exercise>,
)
