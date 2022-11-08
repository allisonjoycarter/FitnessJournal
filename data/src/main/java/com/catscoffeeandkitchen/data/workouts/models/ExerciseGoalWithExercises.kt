package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.Embedded
import androidx.room.Relation


data class ExerciseGoalWithExercises(
    @Embedded val goal: ExerciseGoal,
    @Relation(
        parentColumn = "exerciseId",
        entity = Exercise::class,
        entityColumn = "eId",
    )
    val exercise: Exercise
)
