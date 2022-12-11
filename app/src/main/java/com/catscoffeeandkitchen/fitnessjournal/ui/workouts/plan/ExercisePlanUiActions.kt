package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import com.catscoffeeandkitchen.domain.models.ExpectedSet

interface ExercisePlanUiActions {
    fun addExercise()
    fun addExerciseGroup()
    fun removeExercise(exercise: ExpectedSet)

    fun updateWorkoutName(name: String)
    fun updateWorkoutNotes(notes: String)
    fun updateExercise(setNumber: Int, field: ExercisePlanField, value: Int)

    fun startWorkout()
}