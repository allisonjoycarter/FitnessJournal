package com.catscoffeeandkitchen.data.workouts.util

import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity
import com.catscoffeeandkitchen.data.workouts.models.SetEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseWithStats
import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity as DbExercise
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanEntity as DbWorkoutPlan
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity as DbWorkout


fun ExerciseSet.toDbExerciseSet(
    exerciseId: Long,
    workoutId: Long,
    positionId: Long,
    chosenFromGroup: Long? = null
): SetEntity {
    return SetEntity(
        this.id,
        exerciseId = exerciseId,
        workoutId = workoutId,
        positionId = positionId,
        groupId = chosenFromGroup,
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        repsInReserve = this.repsInReserve,
        perceivedExertion = this.perceivedExertion,
        setNumber = this.setNumber,
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

fun ExpectedSet.toGoal(exerciseId: Long?, groupId: Long?, planId: Long): ExerciseGoal {
    return ExerciseGoal(
        exerciseId = exerciseId,
        exerciseGroupId = groupId,
        workoutPlanId = planId,
        sets = this.sets,
        positionInWorkout = this.positionInWorkout,
        reps = this.reps,
        repRangeMax = this.maxReps,
        repRangeMin = this.minReps,
        weightInPounds = 0f,
        weightInKilograms = 0f,
        repsInReserve = this.rir,
        perceivedExertion = this.perceivedExertion,
        note = this.note,
        modifier = null,
        type = this.type,
    )
}

fun ExerciseGroup.toEntity(): ExerciseGroupEntity {
    return ExerciseGroupEntity(
        gId = id,
        name = this.name
    )
}

fun ExerciseWithStats.toStats(): ExerciseStats {
    return ExerciseStats(
        lastCompletedAt = this.lastCompletedAt,
        amountCompleted = this.amountPerformed,
        amountCompletedThisWeek = this.amountCompletedThisWeek,
        highestWeightInKilograms = this.highestWeightInKilograms,
        highestWeightInPounds = this.highestWeightInPounds
    )
}
