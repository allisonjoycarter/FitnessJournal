package com.catscoffeeandkitchen.data.workouts.models.exercise

import androidx.room.Embedded
import androidx.room.Relation
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity
import com.catscoffeeandkitchen.data.workouts.models.SetEntity

data class PositionWithExerciseAndSets(
    @Embedded var position: ExercisePositionEntity,
    @Relation(
        parentColumn = "exerciseId",
        entity = ExerciseEntity::class,
        entityColumn = "eId",
    )
    val exercise: ExerciseEntity? = null,
    @Relation(
        parentColumn = "groupId",
        entity = ExerciseGroupEntity::class,
        entityColumn = "gId",
    )
    val group: ExerciseGroupEntity? = null,
    @Relation(
        parentColumn = "epId",
        entity = SetEntity::class,
        entityColumn = "positionId",
    )
    val exerciseSets: List<SetEntity> = emptyList(),
)

