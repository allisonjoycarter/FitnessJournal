package com.catscoffeeandkitchen.data.workouts.models

import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import java.time.OffsetDateTime

data class SetAndExerciseCombinedWithCompletedAt(
    val sId: Long,
    val exerciseId: Long,
    val workoutId: Long,
    val reps: Int = 1,
    val weightInPounds: Int = 0,
    val weightInKilograms: Int = 0,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val setNumberInWorkout: Int = 1,
    val isComplete: Boolean = false,
    val type: ExerciseSetType = ExerciseSetType.Working,

    val name: String,
    val musclesWorked: List<String> = emptyList(),
    val category: String? = null,
    val thumbnailUrl: String? = null,

    val completedAt: OffsetDateTime? = null,
)
