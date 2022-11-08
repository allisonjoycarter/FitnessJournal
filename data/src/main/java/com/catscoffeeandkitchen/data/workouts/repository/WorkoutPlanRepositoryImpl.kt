package com.catscoffeeandkitchen.data.workouts.repository

import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.util.toDbWorkoutPlan
import com.catscoffeeandkitchen.data.workouts.util.toExpectedSet
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlan as DbWorkoutPlan
import com.catscoffeeandkitchen.domain.interfaces.WorkoutPlanRepository
import com.catscoffeeandkitchen.domain.models.*
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

class WorkoutPlanRepositoryImpl @Inject constructor(
    private val database: FitnessJournalDb
): WorkoutPlanRepository {
    override suspend fun getWorkoutPlans(): List<WorkoutPlan> {
        return database.workoutPlanDao().getAllWithGoals().map { dbWorkout ->
            Timber.d("got workout ${dbWorkout.plan.wpId}, note = ${dbWorkout.plan.note}")
            WorkoutPlan(
                addedAt = dbWorkout.plan.addedAt,
                name = dbWorkout.plan.name,
                note = dbWorkout.plan.note,
                exercises = dbWorkout.goals.map { goal ->
                    val dbExercise = database.exerciseDao().getExerciseById(goal.exerciseId)
                    goal.toExpectedSet(dbExercise.name, dbExercise.musclesWorked)
                }
            )
        }
    }

    override suspend fun getWorkoutPlanByAddedDate(addedAt: OffsetDateTime): WorkoutPlan {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanWithGoalsByAddedAt(addedAt)
        return WorkoutPlan(
            addedAt = dbWorkout.plan.addedAt,
            name = dbWorkout.plan.name,
            note = dbWorkout.plan.note,
            exercises = dbWorkout.goals.map { goal ->
                val dbExercise = database.exerciseDao().getExerciseById(goal.exerciseId)
                goal.toExpectedSet(dbExercise.name, dbExercise.musclesWorked)
            }
        )
    }

    override suspend fun createWorkoutPlan(plan: WorkoutPlan) {
        database.workoutPlanDao().insert(plan.toDbWorkoutPlan())
    }

    override suspend fun updateWorkoutPlan(plan: WorkoutPlan) {
        val dbPlan = database.workoutPlanDao().getWorkoutPlanByAddedAt(plan.addedAt)
        database.workoutPlanDao().update(plan.toDbWorkoutPlan(dbPlan.wpId))
    }

    override suspend fun addExpectedSetToWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val exercise = database.exerciseDao().getExerciseByName(expectedSet.exercise.name)
        if (exercise != null) {
            Timber.d("adding ${exercise.name} at set ${workout.exercises.size} to workout ${dbWorkout.wpId}")
            val setNumber = if (workout.exercises.isNotEmpty())
                workout.exercises.maxOf { it.setNumberInWorkout } + 1
                else 1
            val id = database.exerciseGoalDao().insert(
                ExerciseGoal(
                    exerciseId = exercise.eId,
                    workoutPlanId = dbWorkout.wpId,
                    sets = expectedSet.sets,
                    reps = expectedSet.reps,
                    repRangeMax = expectedSet.maxReps,
                    repRangeMin = expectedSet.minReps,
                    repsInReserve = expectedSet.rir,
                    perceivedExertion = expectedSet.perceivedExertion,
                    setNumberInWorkout = setNumber,
                )
            )

            Timber.d("successfully added workout goal $id")
        }
    }

    override suspend fun removeExpectedSetFromWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanWithGoalsByAddedAt(workout.addedAt)
        val dbExerciseGoal = database.exerciseGoalDao().getExerciseGoalByWorkoutAndSetNumber(
            dbWorkout.plan.wpId,
            setNumberInWorkout = expectedSet.setNumberInWorkout
        )
        database.exerciseGoalDao().delete(dbExerciseGoal)

        dbWorkout.goals
            .filter{ it.setNumberInWorkout > expectedSet.setNumberInWorkout }
            .forEach { set ->
                database.exerciseGoalDao().update(set.copy(setNumberInWorkout = set.setNumberInWorkout - 1))
            }
    }

    override suspend fun updateExpectedSet(
        workout: WorkoutPlan,
        expectedSet: ExpectedSet
    ) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val dbExerciseGoal = database.exerciseGoalDao().getExerciseGoalByWorkoutAndSetNumber(
            dbWorkout.wpId,
            setNumberInWorkout = expectedSet.setNumberInWorkout
        )
        database.exerciseGoalDao().update(
            ExerciseGoal(
                exerciseId = dbExerciseGoal.exerciseId,
                workoutPlanId = dbWorkout.wpId,
                sets = expectedSet.sets,
                reps = expectedSet.reps,
                repRangeMax = expectedSet.maxReps,
                repRangeMin = expectedSet.minReps,
                repsInReserve = expectedSet.rir,
                perceivedExertion = expectedSet.perceivedExertion,
                setNumberInWorkout = expectedSet.setNumberInWorkout,
            )
        )
    }
}