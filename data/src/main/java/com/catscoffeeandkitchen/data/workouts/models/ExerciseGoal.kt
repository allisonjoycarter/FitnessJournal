package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.catscoffeeandkitchen.domain.models.ExerciseSetModifier
import com.catscoffeeandkitchen.domain.models.ExerciseSetType

@Entity(
    primaryKeys = ["workoutPlanId", "setNumberInWorkout"],
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["eId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutPlan::class,
            parentColumns = ["wpId"],
            childColumns = ["workoutPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exerciseId"]),
        Index(value = ["workoutPlanId"]),
    ]
)
data class ExerciseGoal(
    val exerciseId: Long,
    val workoutPlanId: Long,
    val sets: Int,
    val setNumberInWorkout: Int = 0,
    val reps: Int,
    val repRangeMax: Int = 0,
    val repRangeMin: Int = 0,
    val weightInPounds: Float = 0f,
    val weightInKilograms: Float = 0f,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val note: String = "",
    val modifier: ExerciseSetModifier? = null,
    val type: ExerciseSetType = ExerciseSetType.Working,
)
