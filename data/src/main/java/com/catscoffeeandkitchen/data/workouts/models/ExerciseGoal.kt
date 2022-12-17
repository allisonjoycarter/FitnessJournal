package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.domain.models.ExerciseSetModifier
import com.catscoffeeandkitchen.domain.models.ExerciseSetType

@Entity(
    primaryKeys = ["workoutPlanId", "positionId"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["eId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseGroupEntity::class,
            parentColumns = ["gId"],
            childColumns = ["exerciseGroupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["wpId"],
            childColumns = ["workoutPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExercisePositionEntity::class,
            parentColumns = ["epId"],
            childColumns = ["positionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutPlanId"]),
        Index(value = ["exerciseGroupId"]),
        Index(value = ["exerciseId"]),
    ]
)
data class ExerciseGoal(
    val exerciseId: Long? = null,
    val exerciseGroupId: Long? = null,
    val workoutPlanId: Long,
    val positionId: Long,
    val sets: Int,
    val positionInWorkout: Int = 0,
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
