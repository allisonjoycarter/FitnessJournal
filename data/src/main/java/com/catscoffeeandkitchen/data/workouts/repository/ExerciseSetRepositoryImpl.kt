package com.catscoffeeandkitchen.data.workouts.repository

import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSetPartial
import com.catscoffeeandkitchen.data.workouts.util.populateWeight
import com.catscoffeeandkitchen.data.workouts.util.toDbExerciseSet
import com.catscoffeeandkitchen.data.workouts.util.toExercise
import com.catscoffeeandkitchen.data.workouts.util.toExerciseSet
import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.Workout
import javax.inject.Inject


class ExerciseSetRepositoryImpl@Inject constructor(
    private val database: FitnessJournalDb
): ExerciseSetRepository {

    override suspend fun updateExerciseSet(
        exerciseSet: ExerciseSet
    ) {
        database.exerciseSetDao().updatePartial(
            ExerciseSetPartial(
                sId = exerciseSet.id,
                reps = exerciseSet.reps,
                weightInPounds = exerciseSet.weightInPounds,
                weightInKilograms = exerciseSet.weightInKilograms,
                repsInReserve = exerciseSet.repsInReserve,
                perceivedExertion = exerciseSet.perceivedExertion,
                setNumberInWorkout = exerciseSet.setNumberInWorkout,
                type = exerciseSet.type,
                completedAt = exerciseSet.completedAt
            )
        )
    }

    override suspend fun addExerciseSetWithPopulatedData(
        workout: Workout,
        exercise: Exercise,
        exerciseSet: ExerciseSet,
        expectedSet: ExpectedSet?
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workout.addedAt)
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)

        val lastSet = database.exerciseSetDao().getLastSet(dbExercise!!.eId)

        // use expected data, then last set, then whatever is coming in from exerciseSet
        val exerciseSetWithPreviousData = exerciseSet.copy(
                reps = expectedSet?.reps ?: lastSet?.reps ?: exerciseSet.reps,
                perceivedExertion = expectedSet?.perceivedExertion ?: lastSet?.perceivedExertion ?: exerciseSet.perceivedExertion,
                repsInReserve = expectedSet?.rir ?: lastSet?.repsInReserve ?: exerciseSet.repsInReserve,
                weightInPounds = lastSet?.weightInPounds ?: exerciseSet.weightInPounds,
                weightInKilograms = lastSet?.weightInKilograms ?: exerciseSet.weightInKilograms,
                seconds = lastSet?.seconds ?: exerciseSet.seconds,
            )

        val setsAfter = database.exerciseSetDao().getSetsInWorkout(dbWorkout.wId)
        if (setsAfter.any { it.setNumberInWorkout >= exerciseSet.setNumberInWorkout }) {
            database.exerciseSetDao().updateAll(setsAfter.filter { set ->
                set.setNumberInWorkout >= exerciseSet.setNumberInWorkout }
                .map { set -> set.copy(setNumberInWorkout = set.setNumberInWorkout + 1) }
            )
        }

        database.exerciseSetDao().insert(
            exerciseSetWithPreviousData
                .toDbExerciseSet(
                workoutId = dbWorkout.wId,
                exerciseId = dbExercise.eId
            )
        )
    }

    override suspend fun addExerciseSets(
        workout: Workout,
        exercise: Exercise,
        exerciseSets: List<ExerciseSet>
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workout.addedAt)
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)

        val setNumberToUpdate = exerciseSets.minOf { it.setNumberInWorkout }
        val existingSets = database.exerciseSetDao().getSetsInWorkout(dbWorkout.wId)
        existingSets.filter { it.setNumberInWorkout >= setNumberToUpdate }.forEach { set ->
            database.exerciseSetDao().update(set.copy(
                setNumberInWorkout = setNumberToUpdate + exerciseSets.size
            ))
        }

        database.exerciseSetDao().insertAll(exerciseSets.map { set ->
            set.populateWeight().toDbExerciseSet(dbExercise?.eId ?: 0L, dbWorkout.wId)
        })
    }

    override suspend fun getCompletedSetsForExercise(name: String): List<ExerciseSet> {
        val exercise = database.exerciseDao().getExerciseByName(name) ?: return emptyList()
        return database.exerciseSetDao().getAllCompletedSetsForExercise(exercise.eId).map { combined ->
            combined.set.toExerciseSet(combined.exercise.toExercise())
        }
    }

    override suspend fun changeExerciseForSets(setIds: List<Long>, exercise: Exercise) {
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)
        val dbSets = database.exerciseSetDao().getSetsByIds(setIds)

        if (dbExercise != null) {
            database.exerciseSetDao().updateAll(dbSets.map { it.copy(exerciseId = dbExercise.eId)})
        }
    }
}