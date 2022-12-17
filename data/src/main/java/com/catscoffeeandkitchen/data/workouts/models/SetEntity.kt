package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.domain.models.ExerciseSetModifier
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import java.time.OffsetDateTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["eId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["wId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExercisePositionEntity::class,
            parentColumns = ["epId"],
            childColumns = ["positionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseGroupEntity::class,
            parentColumns = ["gId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("exerciseId"),
        Index("workoutId"),
        Index("positionId"),
        Index("groupId"),
    ]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true) val sId: Long,
    val exerciseId: Long,
    val workoutId: Long,
    val positionId: Long,
    val groupId: Long? = null,
    val reps: Int = 1,
    val weightInPounds: Float = 0f,
    val weightInKilograms: Float = 0f,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val setNumber: Int = 1,
    val completedAt: OffsetDateTime? = null,
    val type: ExerciseSetType = ExerciseSetType.Working,
    val seconds: Int = 0,
    val modifier: ExerciseSetModifier? = null,
)
