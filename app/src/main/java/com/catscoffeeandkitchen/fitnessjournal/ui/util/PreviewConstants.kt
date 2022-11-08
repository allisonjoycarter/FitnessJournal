package com.catscoffeeandkitchen.fitnessjournal.ui.util

import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import java.time.OffsetDateTime

object PreviewConstants {

    val exerciseBicepCurl = Exercise(
        name = "Bicep Curl",
        musclesWorked = listOf("Biceps", "Triceps"),
        category = "Arms"
    )

    val expectedSetBicepCurl = ExpectedSet(
        exercise = exerciseBicepCurl,
        reps = 10,
        sets = 3,
        maxReps = 12,
        minReps = 8,
        perceivedExertion = 7,
        rir = 2,
        setNumberInWorkout = 1,
        note = "Drop set????"
    )

    val bicepCurlSets = listOf(
        ExerciseSet(
            id = 0,
            reps = 10,
            exercise = exerciseBicepCurl,
            setNumberInWorkout = 1,
            weightInPounds = 5,
            weightInKilograms = 3,
            repsInReserve = 100,
            perceivedExertion = 2,
            isComplete = false,
            type = ExerciseSetType.WarmUp
        ),
        ExerciseSet(
            id = 1,
            reps = 10,
            exercise = exerciseBicepCurl,
            setNumberInWorkout = 2,
            weightInPounds = 5,
            weightInKilograms = 3,
            repsInReserve = 100,
            perceivedExertion = 2,
            isComplete = false,
            type = ExerciseSetType.WarmUp
        ),
        ExerciseSet(
            id = 2,
            reps = 10,
            exercise = exerciseBicepCurl,
            setNumberInWorkout = 3,
            weightInPounds = 25,
            weightInKilograms = 12,
            repsInReserve = 3,
            perceivedExertion = 8,
            isComplete = false,
            type = ExerciseSetType.Working
        ),
        ExerciseSet(
            id = 3,
            reps = 10,
            exercise = exerciseBicepCurl,
            setNumberInWorkout = 4,
            weightInPounds = 25,
            weightInKilograms = 12,
            repsInReserve = 3,
            perceivedExertion = 8,
            isComplete = false,
            type = ExerciseSetType.Working
        )
    )
}