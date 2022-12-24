package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import com.catscoffeeandkitchen.domain.models.ExpectedSet
import java.time.DayOfWeek

interface ExercisePlanUiActions {
    fun addExercise()
    fun addExerciseGroup()
    fun removeExercise(exercise: ExpectedSet)

    fun updateWorkoutName(name: String)
    fun updateWorkoutNotes(notes: String)
    fun updateWeekdays(weekdays: List<DayOfWeek>)
    fun updateExercise(setNumber: Int, field: ExercisePlanField, value: Int)

    fun startWorkout()
}