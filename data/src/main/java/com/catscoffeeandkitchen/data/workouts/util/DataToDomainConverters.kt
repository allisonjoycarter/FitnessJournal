package com.catscoffeeandkitchen.data.workouts.util

import com.catscoffeeandkitchen.data.workouts.models.CombinedSetData
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoalWithExercises
import com.catscoffeeandkitchen.data.workouts.models.SetAndExerciseCombined
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import java.time.OffsetDateTime
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSet as DbExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.Exercise as DbExercise

fun DbExerciseSet.toExerciseSet(exerciseName: String, musclesWorked: List<String>): ExerciseSet {
    return ExerciseSet(
        this.sId,
        exercise = Exercise(
            exerciseName,
            musclesWorked,
        ),
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        setNumberInWorkout = this.setNumberInWorkout,
        isComplete = this.isComplete,
        type = this.type,
    )
}

fun SetAndExerciseCombined.toExerciseSet(completedAt: OffsetDateTime? = null): ExerciseSet {
    return ExerciseSet(
        this.sId,
        exercise = Exercise(
            this.name,
            this.musclesWorked,
        ),
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        setNumberInWorkout = this.setNumberInWorkout,
        isComplete = this.isComplete,
        completedAt = completedAt,
        type = this.type,
    )
}

fun ExerciseGoal.toExpectedSet(exerciseName: String, musclesWorked: List<String>): ExpectedSet {
    return ExpectedSet(
        exercise = Exercise(
            exerciseName,
            musclesWorked
        ),
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

fun CombinedSetData.toExerciseSet(completedAt: OffsetDateTime? = null): ExerciseSet {
    return ExerciseSet(
        this.sId,
        exercise = Exercise(
            this.name,
            this.musclesWorked,
        ),
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        setNumberInWorkout = this.setNumberInWorkout,
        isComplete = this.isComplete,
        completedAt = completedAt,
        type = this.type
    )
}

fun ExerciseGoalWithExercises.toExpectedSet(): ExpectedSet {
    return ExpectedSet(
        exercise = Exercise(
            this.exercise.name,
            this.exercise.musclesWorked
        ),
        reps = this.goal.reps,
        sets = this.goal.sets,
        maxReps = this.goal.repRangeMax,
        minReps = this.goal.repRangeMin,
        perceivedExertion = this.goal.perceivedExertion,
        rir = this.goal.repsInReserve,
        setNumberInWorkout = this.goal.setNumberInWorkout,
        note = this.goal.note,
    )
}

fun DbExercise.toExercise(): Exercise {
    return Exercise(
        name = this.name,
        musclesWorked = this.musclesWorked,
        category = this.category,
        thumbnailUrl = this.thumbnailUrl
    )
}
