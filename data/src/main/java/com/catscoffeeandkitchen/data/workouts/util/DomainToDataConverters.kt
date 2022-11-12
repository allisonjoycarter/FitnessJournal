package com.catscoffeeandkitchen.data.workouts.util

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSet as DbExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.Exercise as DbExercise
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlan as DbWorkoutPlan
import com.catscoffeeandkitchen.data.workouts.models.Workout as DbWorkout


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


fun Exercise.toDbExercise(id: Long = 0L, userCreated: Boolean = false): DbExercise {
    return DbExercise(
        eId = id,
        name = this.name,
        musclesWorked = this.musclesWorked,
        userCreated = userCreated,
        category = this.category,
        thumbnailUrl = this.thumbnailUrl,
        equipmentType = this.equipmentType,
    )
}

fun Workout.toDbWorkout(id: Long = 0L, planId: Long? = null): DbWorkout {
    return DbWorkout(
        wId = id,
        planId = planId,
        minutesToComplete = 0,
        addedAt = this.addedAt,
        completedAt = this.completedAt,
        name = this.name,
        note = this.note,
    )
}
