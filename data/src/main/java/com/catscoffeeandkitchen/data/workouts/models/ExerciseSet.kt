package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.catscoffeeandkitchen.domain.models.ExerciseSetType

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["eId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["wId"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("exerciseId"),
        Index("workoutId"),
    ]
)
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true) val sId: Long,
    val exerciseId: Long,
    val workoutId: Long,
    val reps: Int = 1,
    val weightInPounds: Int = 0,
    val weightInKilograms: Int = 0,
    val repsInReserve: Int = 0,
    val perceivedExertion: Int = 0,
    val setNumberInWorkout: Int = 1,
    val isComplete: Boolean = false,
    val type: ExerciseSetType = ExerciseSetType.Working
)
