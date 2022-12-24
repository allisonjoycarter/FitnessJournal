package com.catscoffeeandkitchen.data.workouts.repository

import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGoal
import com.catscoffeeandkitchen.data.workouts.util.*
import com.catscoffeeandkitchen.domain.interfaces.HomeRepository
import com.catscoffeeandkitchen.domain.models.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.OffsetDateTime
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

class HomeRepositoryImpl @Inject constructor(
    private val database: FitnessJournalDb
): HomeRepository {
    override suspend fun getNextWorkoutPlan(): WorkoutPlan? {
        val today = OffsetDateTime.now().dayOfWeek
        val plans = database.workoutPlanDao().getWithDay(today.name)
        val nextPlan = plans.maxByOrNull { it.addedAt }?.toPlan() ?: return null

        val planWithGoals = database.workoutPlanDao().getWorkoutPlanWithGoalsById(nextPlan.id)
        return WorkoutPlan(
            id = planWithGoals.plan.wpId,
            addedAt = planWithGoals.plan.addedAt,
            name = planWithGoals.plan.name,
            note = planWithGoals.plan.note,
            entries = getExpectedSetFromGoals(planWithGoals.goals),
            daysOfWeek = planWithGoals.plan.daysOfWeek.map { DayOfWeek.valueOf(it) }
        )
    }

    override suspend fun getWorkoutsPerWeek(weeks: Int): List<OffsetDateTime> {
        val startDate = OffsetDateTime.now().minusWeeks(weeks.toLong())
        val workouts = database.workoutDao().getWorkoutsAfter(startDate.toUTCEpochMilli())

        return workouts.mapNotNull { it.completedAt }
    }

    override suspend fun getLastExercisesCompleted(): List<WorkoutEntry> {
        val lastWorkout = database.workoutDao().getLastWorkout() ?: return emptyList()
        val sets = database.exercisePositionDao().getPositionsWithExerciseAndSets(lastWorkout.wId)

        return sets.map { data ->
            WorkoutEntry(
                position = data.position.position,
                exercise = data.exercise?.toExercise(),
                expectedSet = null,
                sets = data.exerciseSets.mapNotNull { set ->
                    if (data.exercise == null) null
                    else set.toExerciseSet(data.exercise.toExercise())
                }.sortedBy { it.setNumber }
            )
        }.sortedBy { it.position }
    }

    override suspend fun getMostImprovedExercise(weeks: Int): ExerciseProgressStats? {
        val startDate = OffsetDateTime.now().minusWeeks(weeks.toLong())
        val bestSet = database.exerciseSetDao().getBestSetSince(startDate.toUTCEpochMilli()) ?: return null
        val exercise = database.exerciseDao().getExerciseById(bestSet.exerciseId)

        val worstSet = database.exerciseSetDao().getWorstSetSince(startDate.toUTCEpochMilli(), exercise.eId) ?: return null

        return ExerciseProgressStats(
            exercise = exercise.toExercise(),
            worstSet = worstSet.toExerciseSet(exercise.toExercise()),
            bestSet = bestSet.toExerciseSet(exercise.toExercise()),
            amountOfTime = Duration.between(startDate, OffsetDateTime.now()),
            starting1RM = worstSet.weightInPounds / (1.0278 - 0.0278 * worstSet.reps).toFloat(),
            ending1RM = bestSet.weightInPounds / (1.0278 - 0.0278 * bestSet.reps).toFloat()
        )
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