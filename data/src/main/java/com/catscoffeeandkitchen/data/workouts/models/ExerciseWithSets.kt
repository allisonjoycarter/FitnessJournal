package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Relation
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity

data class ExerciseWithSets(
    @Embedded var exercise: ExerciseEntity,
    @Relation(
        parentColumn = "eId",
        entity = SetEntity::class,
        entityColumn = "exerciseId",
    )
    val exerciseSets: List<SetEntity> = emptyList(),
    )

