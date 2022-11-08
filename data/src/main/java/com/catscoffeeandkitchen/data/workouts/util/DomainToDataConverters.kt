package com.catscoffeeandkitchen.data.workouts.util

import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSet as DbExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlan as DbWorkoutPlan


fun ExerciseSet.toDbExerciseSet(exerciseId: Long, workoutId: Long): DbExerciseSet {
    return DbExerciseSet(
        this.id,
        exerciseId = exerciseId,
        workoutId = workoutId,
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        repsInReserve = this.repsInReserve,
        perceivedExertion = this.perceivedExertion,
        setNumberInWorkout = this.setNumberInWorkout,
        type = this.type
    )
}

fun WorkoutPlan.toDbWorkoutPlan(planId: Long = 0L): DbWorkoutPlan {
    return DbWorkoutPlan(
        wpId = planId,
        addedAt = this.addedAt,
        name = this.name,
        note = this.note
    )
}
