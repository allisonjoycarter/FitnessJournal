package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.ExerciseProgressStats
import com.catscoffeeandkitchen.domain.models.WorkoutEntry
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import java.time.OffsetDateTime

interface HomeRepository {
    suspend fun getNextWorkoutPlan(): WorkoutPlan?

    suspend fun getWorkoutsPerWeek(weeks: Int): List<OffsetDateTime>

    suspend fun getLastExercisesCompleted(): List<WorkoutEntry>

    suspend fun getMostImprovedExercise(weeks: Int): ExerciseProgressStats?
}