package com.catscoffeeandkitchen.data.workouts.models

import androidx.room.*

data class WorkoutPlanWithGoalsAndPosition(
    @Embedded val plan: WorkoutPlanEntity,
    @Relation(
        parentColumn = "wpId",
        entity = ExerciseGoal::class,
        entityColumn = "workoutPlanId",
    )
    val goals: List<ExerciseGoalWithPosition> = emptyList(),
)
