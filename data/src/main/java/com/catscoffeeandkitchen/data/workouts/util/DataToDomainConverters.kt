package com.catscoffeeandkitchen.data.workouts.util

import com.catscoffeeandkitchen.data.workouts.models.CombinedSetData
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoalWithExercises
import com.catscoffeeandkitchen.domain.models.*
import java.time.OffsetDateTime
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSet as DbExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.Workout as DbWorkout
import com.catscoffeeandkitchen.data.workouts.models.Exercise as DbExercise

fun DbExerciseSet.toExerciseSet(exercise: Exercise): ExerciseSet {
    return ExerciseSet(
        this.sId,
        exercise = exercise,
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        setNumberInWorkout = this.setNumberInWorkout,
        isComplete = this.completedAt != null,
        completedAt = this.completedAt,
        type = this.type,
        seconds = this.seconds,
        modifier = this.modifier,
    )
}

fun ExerciseGoal.toExpectedSet(exercise: Exercise): ExpectedSet {
    return ExpectedSet(
        exercise = exercise,
        reps = this.reps,
        sets = this.sets,
        maxReps = this.repRangeMax,
        minReps = this.repRangeMin,
        perceivedExertion = this.perceivedExertion,
        rir = this.repsInReserve,
        setNumberInWorkout = this.setNumberInWorkout,
        note = this.note,
    )
}

fun CombinedSetData.toExerciseSet(): ExerciseSet {
    return ExerciseSet(
        this.sId,
        exercise = Exercise(
            name = this.name,
            musclesWorked = this.musclesWorked,
            category = this.category,
            thumbnailUrl = this.thumbnailUrl,
            equipmentType = this.equipmentType,
        ),
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        setNumberInWorkout = this.setNumberInWorkout,
        isComplete = this.completedAt != null,
        completedAt = this.completedAt,
        type = this.type
    )
}

fun ExerciseGoalWithExercises.toExpectedSet(exercise: Exercise): ExpectedSet {
    return ExpectedSet(
        exercise = exercise,
        reps = this.goal.reps,
        sets = this.goal.sets,
        maxReps = this.goal.repRangeMax,
        minReps = this.goal.repRangeMin,
        perceivedExertion = this.goal.perceivedExertion,
        rir = this.goal.repsInReserve,
        setNumberInWorkout = this.goal.setNumberInWorkout,
        note = this.goal.note,
        type = this.goal.type,
    )
}

fun DbExercise.toExercise(): Exercise {
    return Exercise(
        name = this.name,
        musclesWorked = this.musclesWorked,
        category = this.category,
        thumbnailUrl = this.thumbnailUrl,
        equipmentType = this.equipmentType
    )
}

fun DbWorkout.toWorkout(): Workout {
    return Workout(
        addedAt = this.addedAt,
        name = this.name,
        note = this.note,
        completedAt = this.completedAt,
    )
}
