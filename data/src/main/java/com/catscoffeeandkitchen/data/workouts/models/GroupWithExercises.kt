package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity

data class GroupWithExercises(
    @Embedded val group: ExerciseGroupEntity,
    @Relation(
        parentColumn = "gId",
        entity = ExerciseEntity::class,
        entityColumn = "eId",
        associateBy = Junction(
            value = GroupExerciseXRef::class,
            parentColumn = "groupId",
            entityColumn = "exerciseId")
    )
    val exercises: List<ExerciseEntity> = emptyList(),
)
