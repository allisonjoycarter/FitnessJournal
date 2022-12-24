package com.catscoffeeandkitchen.data.workouts.repository

import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.data.workouts.util.*
import com.catscoffeeandkitchen.data.workouts.models.WorkoutPlanEntity as DbWorkoutPlan
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity as DbExercise
import com.catscoffeeandkitchen.domain.interfaces.WorkoutPlanRepository
import com.catscoffeeandkitchen.domain.models.*
import timber.log.Timber
import java.time.DayOfWeek
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
                id = dbWorkout.plan.wpId,
                addedAt = dbWorkout.plan.addedAt,
                name = dbWorkout.plan.name,
                note = dbWorkout.plan.note,
                entries = getExpectedSetFromGoals(dbWorkout.goals),
                daysOfWeek = dbWorkout.plan.daysOfWeek.map { DayOfWeek.valueOf(it) }
            )
        }
    }

    override suspend fun getWorkoutPlan(id: Long): WorkoutPlan {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanWithGoalsById(id)
        return WorkoutPlan(
            id = dbWorkout.plan.wpId,
            addedAt = dbWorkout.plan.addedAt,
            name = dbWorkout.plan.name,
            note = dbWorkout.plan.note,
            entries = getExpectedSetFromGoals(dbWorkout.goals),
            daysOfWeek = dbWorkout.plan.daysOfWeek.map { DayOfWeek.valueOf(it) }
        )
    }

    override suspend fun createWorkoutPlan(plan: WorkoutPlan) {
        database.workoutPlanDao().insert(plan.toEntity())
    }

    override suspend fun createPlanFromWorkout(workout: Workout): OffsetDateTime {
        val workoutEntity = database.workoutDao().getWorkoutByAddedAt(workout.addedAt)
        val planToAdd = DbWorkoutPlan(
            wpId = 0L,
            addedAt = OffsetDateTime.now(),
            name = "Plan from ${workout.name}",
        )
        val planId = database.workoutPlanDao().insert(planToAdd)
        val workoutPositions = database.exercisePositionDao().getPositionsWithExerciseAndSets(workoutEntity.wId)

        val dbExercises = arrayListOf<DbExercise>()
        val goals = workoutPositions.map { item ->
            item.exercise?.let { dbExercises.add(it) }
            val position = workoutPositions.first { it.position.epId == item.position.epId }
            val positionId = database.exercisePositionDao().insert(
                position.position.copy(epId = 0L, planId = planId)
            )

            ExerciseGoal(
                egId = 0L,
                exerciseId = item.exercise?.eId ?: 0L,
                workoutPlanId = planId,
                sets = item.exerciseSets.size,
                positionId = positionId,
                reps = item.exerciseSets.map { it.reps }.average().roundToInt(),
                repRangeMax = item.exerciseSets.minOf { it.reps },
                repRangeMin = item.exerciseSets.maxOf { it.reps },
                weightInPounds = item.exerciseSets.map { it.weightInPounds }.average().toFloat(),
                weightInKilograms = item.exerciseSets.map { it.weightInKilograms }.average().toFloat(),
                repsInReserve = item.exerciseSets.map { it.repsInReserve }.average().roundToInt(),
                perceivedExertion = item.exerciseSets.map { it.perceivedExertion }.average().roundToInt(),
                note = "",
            )
        }
        database.exerciseGoalDao().insertAll(goals)

        return planToAdd.addedAt
    }

    override suspend fun updateWorkoutPlan(plan: WorkoutPlan) {
        val dbPlan = database.workoutPlanDao().getById(plan.id)
        database.workoutPlanDao().update(plan.toEntity(dbPlan.wpId))
    }

    override suspend fun addExpectedSetToWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet) {
        val dbWorkout = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val exercise = expectedSet.exercise?.let { database.exerciseDao().getExerciseByName(it.name) }
        Timber.d("adding ${exercise?.name} at set ${workout.entries.size} to workout ${dbWorkout.wpId}")
        val setNumber = if (workout.entries.isNotEmpty())
            workout.entries.maxOf { it.positionInWorkout } + 1
            else 1

        val positionId = database.exercisePositionDao().insert(
            ExercisePositionEntity(
                epId = 0L,
                workoutId = null,
                planId = dbWorkout.wpId,
                position = setNumber,
                exerciseId = exercise?.eId,
                groupId = expectedSet.exerciseGroup?.id
            )
        )

        val id = database.exerciseGoalDao().insert(
            ExerciseGoal(
                egId = 0L,
                exerciseId = exercise?.eId,
                exerciseGroupId = expectedSet.exerciseGroup?.id,
                workoutPlanId = dbWorkout.wpId,
                sets = expectedSet.sets,
                reps = expectedSet.reps,
                repRangeMax = expectedSet.maxReps,
                repRangeMin = expectedSet.minReps,
                repsInReserve = expectedSet.rir,
                perceivedExertion = expectedSet.perceivedExertion,
                positionId = positionId,
            )
        )
        Timber.d("successfully added workout goal $id")
    }

    override suspend fun removeExpectedSetFromWorkout(workout: WorkoutPlan, expectedSet: ExpectedSet) {
        val planWithGoals = database.workoutPlanDao().getWithGoalsByAddedAt(workout.addedAt)
        val positions = database.exercisePositionDao().getPositionsInPlan(planWithGoals.plan.wpId)

        val dbExerciseGoal = database.exerciseGoalDao().getByPlanAndPositionId(
            planId = planWithGoals.plan.wpId,
            positionId = positions.first { it.position == expectedSet.positionInWorkout }.epId
        )
        database.exerciseGoalDao().delete(dbExerciseGoal)

        val updatedPositions = positions
            .filter { it.position > expectedSet.positionInWorkout }.map { entry ->
                entry.copy(position = entry.position - 1)
            }

        database.exercisePositionDao().updateAll(updatedPositions)
    }

    override suspend fun updateExpectedSet(
        workout: WorkoutPlan,
        expectedSet: ExpectedSet
    ) {
        val planEntity = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val position = database.exercisePositionDao().getPositionInPlanByPosition(
            planEntity.wpId,
            position = expectedSet.positionInWorkout
        )

        val dbExerciseGoal = database.exerciseGoalDao().getByPlanAndPositionId(
            planEntity.wpId,
            positionId = position.epId
        )

        database.exerciseGoalDao().update(
            expectedSet.toGoal(
                exerciseId = dbExerciseGoal.exerciseId,
                groupId = dbExerciseGoal.exerciseGroupId,
                planId = planEntity.wpId,
                positionId = dbExerciseGoal.positionId
            )
        )
    }

    override suspend fun updateExpectedSetPosition(
        workout: WorkoutPlan,
        expectedSet: ExpectedSet,
        newPosition: Int
    ) {
        val planEntity = database.workoutPlanDao().getWorkoutPlanByAddedAt(workout.addedAt)
        val positions = database.exercisePositionDao().getPositionsInPlan(planEntity.wpId)

        val movingUp = expectedSet.positionInWorkout > newPosition
        positions.find { it.position == newPosition }?.let { entry ->
            Timber.d("*** updating ${entry.position} to " +
                    "${if (movingUp) (entry.position + 1) else (entry.position - 1)}")
            database.exercisePositionDao().update(entry.copy(
                position = if (movingUp) (entry.position + 1) else (entry.position - 1)
            ))
        }

        positions.find { it.position == expectedSet.positionInWorkout }?.let { positionToMove ->
            database.exercisePositionDao().update(positionToMove.copy(position = newPosition))
        }
    }

    private suspend fun getExpectedSetFromGoals(goals: List<ExerciseGoal>): List<ExpectedSet> {
        return goals.mapNotNull { goal ->
            val position = database.exercisePositionDao().getPosition(goal.positionId)
            when {
                goal.exerciseId != null -> {
                    val dbExercise = database.exerciseDao().getExerciseById(goal.exerciseId)
                    goal.toExpectedSet(
                        position = position.position,
                        exercise = dbExercise.toExercise(position.position)
                    )
                }
                goal.exerciseGroupId != null -> {
                    val dbExerciseGroup =
                        database.exerciseGroupDao().getGroupByIdWithExercises(goal.exerciseGroupId)

                    goal.toExpectedSet(
                        position = position.position,
                        group = dbExerciseGroup.group.toExerciseGroup(
                            exercises = dbExerciseGroup.exercises.map { it.toExercise(position.position) },
                        )
                    )
                }
                else -> null
            }
        }.sortedBy { it.positionInWorkout }
    }
}