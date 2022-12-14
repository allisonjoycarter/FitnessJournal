package com.catscoffeeandkitchen.data.workouts.repository

import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.util.*
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanEntity as DbWorkoutPlan
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity as DbExercise
import com.catscoffeeandkitchen.domain.interfaces.WorkoutPlanRepository
import com.catscoffeeandkitchen.domain.models.*
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlin.math.roundToInt

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
                exercises = getExpectedSetFromGoals(dbWorkout.goals, database)
            )
        }
    }

    override suspend fun getWorkoutPlanByAddedDate(addedAt: OffsetDateTime): WorkoutPlan {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanWithGoalsByAddedAt(addedAt)
        return WorkoutPlan(
            addedAt = dbWorkout.plan.addedAt,
            name = dbWorkout.plan.name,
            note = dbWorkout.plan.note,
            exercises = getExpectedSetFromGoals(dbWorkout.goals, database)
        )
    }

    override suspend fun createWorkoutPlan(plan: WorkoutPlan) {
        database.workoutPlanDao().insert(plan.toDbWorkoutPlan())
    }

    override suspend fun createPlanFromWorkout(workout: Workout): OffsetDateTime {
        val planToAdd = DbWorkoutPlan(
            wpId = 0L,
            addedAt = OffsetDateTime.now(),
            name = "Plan from ${workout.name}",
        )
        val planId = database.workoutPlanDao().insert(planToAdd)

        var setNumber = 0
        val dbExercises = arrayListOf<DbExercise>()
        val goals = workout.sets
            .groupBy { it.exercise.name }.map { item ->
                val exercise = database.exerciseDao().getExerciseByName(item.key)
                exercise?.let { dbExercises.add(it) }
                setNumber++

                ExerciseGoal(
                    exerciseId = exercise?.eId ?: 0L,
                    workoutPlanId = planId,
                    sets = item.value.size,
                    positionInWorkout = setNumber,
                    reps = item.value.map { it.reps }.average().roundToInt(),
                    repRangeMax = item.value.minOf { it.reps },
                    repRangeMin = item.value.maxOf { it.reps },
                    weightInPounds = item.value.map { it.weightInPounds }.average().toFloat(),
                    weightInKilograms = item.value.map { it.weightInKilograms }.average().toFloat(),
                    repsInReserve = item.value.map { it.repsInReserve }.average().roundToInt(),
                    perceivedExertion = item.value.map { it.perceivedExertion }.average().roundToInt(),
                    note = "",
                )
        }
        database.exerciseGoalDao().insertAll(goals)

        return planToAdd.addedAt
    }

    override suspend fun updateWorkoutPlan(plan: WorkoutPlan) {
        val dbPlan = database.workoutPlanDao().getWorkoutPlanByAddedAt(plan.addedAt)
        database.workoutPlanDao().update(plan.toDbWorkoutPlan(dbPlan.wpId))
    }

    override suspend fun addExpectedSetToWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val exercise = expectedSet.exercise?.let { database.exerciseDao().getExerciseByName(it.name) }
        Timber.d("adding ${exercise?.name} at set ${workout.exercises.size} to workout ${dbWorkout.wpId}")
        val setNumber = if (workout.exercises.isNotEmpty())
            workout.exercises.maxOf { it.positionInWorkout } + 1
            else 1
        val id = database.exerciseGoalDao().insert(
            ExerciseGoal(
                exerciseId = exercise?.eId,
                exerciseGroupId = expectedSet.exerciseGroup?.id,
                workoutPlanId = dbWorkout.wpId,
                sets = expectedSet.sets,
                reps = expectedSet.reps,
                repRangeMax = expectedSet.maxReps,
                repRangeMin = expectedSet.minReps,
                repsInReserve = expectedSet.rir,
                perceivedExertion = expectedSet.perceivedExertion,
                positionInWorkout = setNumber,
            )
        )
        Timber.d("successfully added workout goal $id")
    }

    override suspend fun removeExpectedSetFromWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanWithGoalsByAddedAt(workout.addedAt)
        val dbExerciseGoal = database.exerciseGoalDao().getExerciseGoalByWorkoutAndSetNumber(
            workoutId = dbWorkout.plan.wpId,
            position = expectedSet.positionInWorkout
        )
        database.exerciseGoalDao().delete(dbExerciseGoal)

        dbWorkout.goals
            .filter{ it.positionInWorkout > expectedSet.positionInWorkout }
            .forEach { set ->
                database.exerciseGoalDao().update(set.copy(positionInWorkout = set.positionInWorkout - 1))
            }
    }

    override suspend fun updateExpectedSet(
        workout: WorkoutPlan,
        expectedSet: ExpectedSet
    ) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val dbExerciseGoal = database.exerciseGoalDao().getExerciseGoalByWorkoutAndSetNumber(
            dbWorkout.wpId,
            position = expectedSet.positionInWorkout
        )

        database.exerciseGoalDao().update(
            ExerciseGoal(
                exerciseId = dbExerciseGoal.exerciseId,
                exerciseGroupId = dbExerciseGoal.exerciseGroupId,
                workoutPlanId = dbWorkout.wpId,
                sets = expectedSet.sets,
                reps = expectedSet.reps,
                repRangeMax = expectedSet.maxReps,
                repRangeMin = expectedSet.minReps,
                repsInReserve = expectedSet.rir,
                perceivedExertion = expectedSet.perceivedExertion,
                positionInWorkout = expectedSet.positionInWorkout,
            )
        )
    }

    override suspend fun updateExpectedSetPosition(
        workout: WorkoutPlan,
        expectedSet: ExpectedSet,
        newSetNumber: Int
    ) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val dbExerciseGoals = database.exerciseGoalDao().getGoalsInWorkout(
            dbWorkout.wpId,
        )
        val goalToUpdate = dbExerciseGoals.find { entry ->
            entry.goal.positionInWorkout == expectedSet.positionInWorkout
        }
        val movingDown = expectedSet.positionInWorkout < newSetNumber

        database.exerciseGoalDao().updateAll(dbExerciseGoals
            .filter { goal ->
                if (movingDown) {
                    goal.goal.positionInWorkout >= newSetNumber
                } else {
                    goal.goal.positionInWorkout <= newSetNumber
                }
            }
            .map { item ->
                val updatedSetNumber = if (movingDown)
                    item.goal.positionInWorkout - 1
                else
                    item.goal.positionInWorkout + 1
                item.goal.copy(
                    positionInWorkout = updatedSetNumber
                )
        })

        goalToUpdate?.let { goal ->
            database.exerciseGoalDao().update(
                expectedSet.toGoal(
                    goal.exercise.eId,
                    goal.goal.exerciseGroupId,
                    dbWorkout.wpId,
                ).copy(positionInWorkout = newSetNumber)
            )
        }
    }

    private suspend fun getExpectedSetFromGoals(goals: List<ExerciseGoal>, database: FitnessJournalDb): List<ExpectedSet> = goals.mapNotNull { goal ->
        when {
            goal.exerciseId != null -> {
                val dbExercise = database.exerciseDao().getExerciseById(goal.exerciseId)
                goal.toExpectedSet(exercise = dbExercise.toExercise(goal.positionInWorkout))
            }
            goal.exerciseGroupId != null -> {
                val dbExerciseGroup = database.exerciseGroupDao().getGroupByIdWithExercises(goal.exerciseGroupId)

                goal.toExpectedSet(group = dbExerciseGroup.group.toExerciseGroup(
                    exercises = dbExerciseGroup.exercises.map { it.toExercise(goal.positionInWorkout) },
                ))
            }
            else -> null
        }
    }
}