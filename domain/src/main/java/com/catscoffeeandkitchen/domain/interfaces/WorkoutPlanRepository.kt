package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import java.time.OffsetDateTime

interface WorkoutPlanRepository {

    suspend fun getWorkoutPlans(): List<WorkoutPlan>

    suspend fun getWorkoutPlanByAddedDate(addedAt: OffsetDateTime): WorkoutPlan

    suspend fun createWorkoutPlan(plan: WorkoutPlan)

    suspend fun updateWorkoutPlan(plan: WorkoutPlan)

    suspend fun addExpectedSetToWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet)

    suspend fun removeExpectedSetFromWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet)

    suspend fun updateExpectedSet(workout: WorkoutPlan, expectedSet: ExpectedSet)
}