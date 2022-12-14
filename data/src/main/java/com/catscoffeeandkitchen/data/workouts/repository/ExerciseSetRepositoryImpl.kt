package com.catscoffeeandkitchen.data.workouts.repository

import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionInWorkout
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSetPartial
import com.catscoffeeandkitchen.data.workouts.util.populateWeight
import com.catscoffeeandkitchen.data.workouts.util.toDbExerciseSet
import com.catscoffeeandkitchen.data.workouts.util.toExercise
import com.catscoffeeandkitchen.data.workouts.util.toExerciseSet
import com.catscoffeeandkitchen.domain.interfaces.ExerciseSetRepository
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject


class ExerciseSetRepositoryImpl@Inject constructor(
    private val database: FitnessJournalDb
): ExerciseSetRepository {

    override suspend fun updateExerciseSet(
        exerciseSet: ExerciseSet
    ): ExerciseSet {
        database.exerciseSetDao().updatePartial(
            ExerciseSetPartial(
                sId = exerciseSet.id,
                reps = exerciseSet.reps,
                weightInPounds = exerciseSet.weightInPounds,
                weightInKilograms = exerciseSet.weightInKilograms,
                repsInReserve = exerciseSet.repsInReserve,
                perceivedExertion = exerciseSet.perceivedExertion,
                setNumber = exerciseSet.setNumber,
                type = exerciseSet.type,
                completedAt = exerciseSet.completedAt
            )
        )

        return exerciseSet
    }

    override suspend fun addExerciseSetWithPopulatedData(
        workoutAddedAt: OffsetDateTime,
        exerciseName: String,
        exerciseSet: ExerciseSet,
        expectedSet: ExpectedSet?
    ): ExerciseSet {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbExercise = database.exerciseDao().getExerciseByName(exerciseName)
        val dbSets = database.exerciseSetDao().getSetsInWorkout(dbWorkout.wId)

        val lastSet = database.exerciseSetDao().getLastSetOfExerciseInWorkout(dbExercise!!.eId, dbWorkout.wId)

        // use expected data, then last set, then whatever is coming in from exerciseSet
        val exerciseSetWithPreviousData = exerciseSet.copy(
                reps = expectedSet?.reps?.takeIf { it > 0 } ?: lastSet?.reps?.takeIf { it > 0 } ?: exerciseSet.reps,
                perceivedExertion = expectedSet?.perceivedExertion?.takeIf { it > 0 } ?: lastSet?.perceivedExertion?.takeIf { it > 0 } ?: exerciseSet.perceivedExertion,
                repsInReserve = expectedSet?.rir?.takeIf { it > 0 } ?: lastSet?.repsInReserve?.takeIf { it > 0 } ?: exerciseSet.repsInReserve,
                weightInPounds = lastSet?.weightInPounds?.takeIf { it > 0 } ?: exerciseSet.weightInPounds,
                weightInKilograms = lastSet?.weightInKilograms?.takeIf { it > 0 } ?: exerciseSet.weightInKilograms,
                seconds = lastSet?.seconds?.takeIf { it > 0 } ?: exerciseSet.seconds,
                setNumber = if (exerciseSet.setNumber == 0)
                        (dbSets.maxOfOrNull { it.setNumber } ?: 0) + 1
                    else
                        exerciseSet.setNumber
            )

        val position = database.exercisePositionDao()
            .getPositionsInWorkoutWithExerciseId(dbWorkout.wId, dbExercise.eId)

        var exercisePosition = position.firstOrNull()?.position
        var positionId = position.firstOrNull()?.epId
        if (positionId == null) {
            val allPositions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)
            positionId = database.exercisePositionDao().insert(
                ExercisePositionInWorkout(
                epId = 0L,
                exerciseId = dbExercise.eId,
                workoutId = dbWorkout.wId,
                position = allPositions.size + 1
            ))
            exercisePosition = allPositions.size + 1
        }


        val newId = database.exerciseSetDao().insert(
            exerciseSetWithPreviousData
                .toDbExerciseSet(
                workoutId = dbWorkout.wId,
                exerciseId = dbExercise.eId,
                positionId = positionId
            )
        )

        return exerciseSetWithPreviousData.copy(
            id = newId,
            exercise = dbExercise.toExercise(position = exercisePosition)
        )
    }

    override suspend fun addExerciseSets(
        workoutAddedAt: OffsetDateTime,
        exercise: Exercise,
        exerciseSets: List<ExerciseSet>
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)

        val existingSets = database.exerciseSetDao().getSetsInWorkout(dbWorkout.wId)
        existingSets
            .filter { it.exerciseId == dbExercise?.eId }
            .forEachIndexed { index, set ->
                Timber.d("*** updating set ${set.reps}@${set.weightInPounds} at ${set.setNumber} to ${(exerciseSets.size + 1) + index}")
                database.exerciseSetDao().update(set.copy(
                    setNumber = (exerciseSets.size + 1) + index
                ))
            }

        val position = database.exercisePositionDao()
            .getPositionsInWorkoutWithExerciseId(dbWorkout.wId, dbExercise?.eId ?: 0L)

        database.exerciseSetDao().insertAll(exerciseSets.map { set ->
            set.populateWeight().toDbExerciseSet(
                exerciseId = dbExercise?.eId ?: 0L,
                workoutId = dbWorkout.wId,
                positionId = position.first().epId
            )
        })
    }

    override suspend fun getCompletedSetsForExercise(name: String): List<ExerciseSet> {
        val exercise = database.exerciseDao().getExerciseByName(name) ?: return emptyList()
        return database.exerciseSetDao().getAllCompletedSetsForExercise(exercise.eId).map { combined ->
            combined.set.toExerciseSet(combined.exercise.toExercise())
        }
    }

    override suspend fun changeExerciseForSets(
        setIds: List<Long>,
        exercise: Exercise,
        position: Int,
        workoutAddedAt: OffsetDateTime
    ): Exercise {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)
        val dbSets = database.exerciseSetDao().getSetsByIds(setIds)
        val dbPositions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)

        Timber.d("*** dbExercise = ${dbExercise?.name}")
        if (dbExercise != null) {
            dbPositions.firstOrNull { it.position == position }?.let { pos ->
                database.exercisePositionDao().update(pos.copy(exerciseId = dbExercise.eId))

                database.exerciseSetDao().updateAll(dbSets.map { it.copy(exerciseId = dbExercise.eId)})
                return dbExercise.toExercise(position = pos.position)
            }
        }
        return exercise
    }
}