package com.catscoffeeandkitchen.domain.models

data class CompletedExerciseSet(
    val exercise: Exercise,
    val sets: List<ExerciseSet>
)
