package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.*

data class WorkoutWithPlanAndGoals(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "planId",
        entity = WorkoutPlan::class,
        entityColumn = "wpId"
    )
    val plan: WorkoutPlan? = null,
    @Relation(
        parentColumn = "planId",
        entity = ExerciseGoal::class,
        entityColumn = "workoutPlanId",
    )
    val goals: List<ExerciseGoalWithExercises> = emptyList(),
)
