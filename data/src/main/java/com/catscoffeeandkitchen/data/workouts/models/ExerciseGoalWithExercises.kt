package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Relation
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity


data class ExerciseGoalWithExercises(
    @Embedded val goal: ExerciseGoal,
    @Relation(
        parentColumn = "exerciseId",
        entity = ExerciseEntity::class,
        entityColumn = "eId",
    )
    val exercise: ExerciseEntity
)
