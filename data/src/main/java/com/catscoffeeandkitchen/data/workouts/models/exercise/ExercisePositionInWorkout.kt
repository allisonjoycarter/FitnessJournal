package com.catscoffeeandkitchen.data.workouts.models.exercise

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            childColumns = ["exerciseId"],
            parentColumns = ["eId"],
            entity = ExerciseEntity::class,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            childColumns = ["groupId"],
            parentColumns = ["gId"],
            entity = ExerciseGroupEntity::class,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            childColumns = ["workoutId"],
            parentColumns = ["wId"],
            entity = WorkoutEntity::class,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("exerciseId"),
        Index("workoutId"),
    ]
)
data class ExercisePositionInWorkout(
    @PrimaryKey(autoGenerate = true) val epId: Long,
    val workoutId: Long,
    val position: Int = 1,
    val exerciseId: Long ? = null,
    val groupId: Long? = null,
)