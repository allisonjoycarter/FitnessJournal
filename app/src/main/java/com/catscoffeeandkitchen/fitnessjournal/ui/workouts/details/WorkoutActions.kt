package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import kotlinx.coroutines.Job

interface WorkoutActions {
    fun updateName(name: String)
    fun updateNote(note: String?)
    fun finish()
    fun createPlanFromWorkout()
}