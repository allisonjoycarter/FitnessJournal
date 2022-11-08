package com.catscoffeeandkitchen.data.workouts.repository

import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSetPartial
import com.catscoffeeandkitchen.data.workouts.util.toDbExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSet as DbExerciseSet
import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import java.time.OffsetDateTime
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
                isComplete = exerciseSet.isComplete
            )
        )
    }

    override suspend fun addExerciseSet(
        workout: Workout,
        exercise: Exercise,
        exerciseSet: ExerciseSet
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workout.addedAt)
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)

        database.exerciseSetDao().insert(
            exerciseSet.toDbExerciseSet(
                workoutId = dbWorkout.wId,
                exerciseId = dbExercise?.eId ?: 0L
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
            set.toDbExerciseSet(dbExercise?.eId ?: 0L, dbWorkout.wId)
        })
    }

    override suspend fun getCompletedSetsForExercise(name: String): List<ExerciseSet> {
        return database.exerciseSetDao().getAllCompletedSetsForExercise(name).map { combined ->
            ExerciseSet(
                id = combined.sId,
                reps = combined.reps,
                exercise = Exercise(
                    name = combined.name,
                    musclesWorked = combined.musclesWorked,
                    category = combined.category,
                    thumbnailUrl = combined.thumbnailUrl
                ),
                setNumberInWorkout = combined.setNumberInWorkout,
                weightInPounds = combined.weightInPounds,
                weightInKilograms = combined.weightInKilograms,
                repsInReserve = combined.repsInReserve,
                perceivedExertion = combined.perceivedExertion,
                isComplete = combined.isComplete,
                completedAt = combined.completedAt,
                type = combined.type
            )
        }
    }
}