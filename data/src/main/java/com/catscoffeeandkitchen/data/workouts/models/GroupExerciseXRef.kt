package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity

@Entity(
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
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("exerciseId"),
        Index("groupId"),
    ]
)
data class GroupExerciseXRef(
    @PrimaryKey(autoGenerate = true) val egxrId: Long,
    val exerciseId: Long,
    val groupId: Long,
)
