package com.catscoffeeandkitchen.domain.interfaces

import com.catscoffeeandkitchen.domain.models.ExerciseProgressStats
import com.catscoffeeandkitchen.domain.models.WorkoutEntry
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.models.WorkoutWeekStats

interface HomeRepository {
    suspend fun getNextWorkoutPlan(): WorkoutPlan?

    suspend fun getWorkoutWeekStats(weeks: Int): WorkoutWeekStats

    suspend fun getLastExercisesCompleted(): List<WorkoutEntry>

    suspend fun getMostImprovedExercise(weeks: Int): ExerciseProgressStats?
}