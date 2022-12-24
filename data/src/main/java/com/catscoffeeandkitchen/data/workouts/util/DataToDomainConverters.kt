package com.catscoffeeandkitchen.data.workouts.util

import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.domain.models.*
import java.time.DayOfWeek
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity as DbWorkout
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity as DbExercise
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity as DbExerciseGroup

fun SetEntity.toExerciseSet(exercise: Exercise): ExerciseSet {
    return ExerciseSet(
        this.sId,
        exercise = exercise,
        chosenFromGroup = this.groupId,
        reps = this.reps,
        weightInKilograms = this.weightInKilograms,
        weightInPounds = this.weightInPounds,
        setNumber = this.setNumber,
        isComplete = this.completedAt != null,
        completedAt = this.completedAt,
        type = this.type,
        seconds = this.seconds,
        modifier = this.modifier,
        repsInReserve = this.repsInReserve,
        perceivedExertion = this.perceivedExertion
    )
}

fun ExerciseGoal.toExpectedSet(position: Int, exercise: Exercise? = null, group: ExerciseGroup? = null): ExpectedSet {
    return ExpectedSet(
        exercise = exercise,
        exerciseGroup = group,
        reps = this.reps,
        sets = this.sets,
        maxReps = this.repRangeMax,
        minReps = this.repRangeMin,
        perceivedExertion = this.perceivedExertion,
        rir = this.repsInReserve,
        positionInWorkout = position,
        note = this.note,
    )
}

fun ExerciseGoalWithExercises.toExpectedSet(position: Int, exercise: Exercise): ExpectedSet {
    return ExpectedSet(
        exercise = exercise,
        reps = this.goal.reps,
        sets = this.goal.sets,
        maxReps = this.goal.repRangeMax,
        minReps = this.goal.repRangeMin,
        perceivedExertion = this.goal.perceivedExertion,
        rir = this.goal.repsInReserve,
        positionInWorkout = position,
        note = this.goal.note,
        type = this.goal.type,
    )
}

fun DbExerciseGroup.toExerciseGroup(exercises: List<Exercise>): ExerciseGroup {
    return ExerciseGroup(
        id = this.gId,
        name = this.name.orEmpty(),
        exercises = exercises,
    )
}

fun DbExercise.toExercise(amountOfSets: Int? = null, stats: ExerciseStats? = null): Exercise {
    return Exercise(
        name = this.name,
        musclesWorked = this.musclesWorked,
        category = this.category,
        thumbnailUrl = this.thumbnailUrl,
        equipmentType = this.equipmentType,
        amountOfSets = amountOfSets,
        stats = stats,
    )
}

fun DbWorkout.toWorkout(): Workout {
    return Workout(
        id = this.wId,
        addedAt = this.addedAt,
        name = this.name,
        note = this.note,
        completedAt = this.completedAt,
    )
}

fun WorkoutPlanEntity.toPlan(entries: List<ExpectedSet> = emptyList()): WorkoutPlan {
    return WorkoutPlan(
        id = this.wpId,
        addedAt = this.addedAt,
        name = this.name,
        note = this.note,
        entries = entries,
        daysOfWeek = this.daysOfWeek.map { DayOfWeek.valueOf(it) }
    )
}
