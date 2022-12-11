package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.*

data class WorkoutWithPlanAndGoals(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        parentColumn = "planId",
        entity = WorkoutPlanEntity::class,
        entityColumn = "wpId"
    )
    val plan: WorkoutPlanEntity? = null,
    @Relation(
        parentColumn = "planId",
        entity = ExerciseGoal::class,
        entityColumn = "workoutPlanId",
    )
    val goals: List<ExerciseGoalWithExercises> = emptyList(),
)
