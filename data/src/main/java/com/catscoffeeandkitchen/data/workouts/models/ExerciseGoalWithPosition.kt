package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Relation
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity
import com.catscoffeeandkitchen.data.workouts.models.SetEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity

data class ExerciseGoalWithPosition(
    @Embedded val goal: ExerciseGoal,
    @Relation(
        parentColumn = "positionId",
        entity = ExercisePositionEntity::class,
        entityColumn = "epId",
    )
    var position: ExercisePositionEntity,
)

