package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.*

data class WorkoutPlanWithGoals(
    @Embedded val plan: WorkoutPlan,
    @Relation(
        parentColumn = "wpId",
        entity = ExerciseGoal::class,
        entityColumn = "workoutPlanId",
    )
    val goals: List<ExerciseGoal> = emptyList(),
)
