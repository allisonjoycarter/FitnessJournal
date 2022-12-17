package com.catscoffeeandkitchen.data.workouts.models.exercise

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanEntity

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
        ),
        ForeignKey(
            childColumns = ["planId"],
            parentColumns = ["wpId"],
            entity = WorkoutPlanEntity::class,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("exerciseId"),
        Index("planId"),
        Index("groupId"),
        Index("workoutId"),
    ]
)
data class ExercisePositionEntity(
    @PrimaryKey(autoGenerate = true) val epId: Long,
    val workoutId: Long? = null,
    val planId: Long? = null,
    val position: Int = 1,
    val exerciseId: Long? = null,
    val groupId: Long? = null,
)