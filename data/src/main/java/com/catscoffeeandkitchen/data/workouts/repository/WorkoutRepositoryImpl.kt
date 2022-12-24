package com.catscoffeeandkitchen.data.workouts.repository

import androidx.paging.*
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSetPartial
import com.catscoffeeandkitchen.data.workouts.models.SetEntity
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity
import com.catscoffeeandkitchen.data.workouts.util.*
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.DayOfWeek
import java.time.OffsetDateTime

@OptIn(ExperimentalPagingApi::class)
class WorkoutRepositoryImpl(
    val database: FitnessJournalDb,
): WorkoutRepository {
    override suspend fun getWorkouts(): List<Workout> {
        return database.workoutDao().getWorkoutsWithPlans().map { dbWorkout ->
            val entryData = database.exercisePositionDao()
                .getPositionsWithExerciseAndSets(dbWorkout.workout.wId)

            Workout(
                id = dbWorkout.workout.wId,
                name = dbWorkout.workout.name,
                note = dbWorkout.workout.note,
                completedAt = dbWorkout.workout.completedAt,
                addedAt = dbWorkout.workout.addedAt,
                plan = if (dbWorkout.plan == null) null else
                    WorkoutPlan(
                        id = dbWorkout.plan.wpId,
                        addedAt = dbWorkout.plan.addedAt,
                        name = dbWorkout.plan.name,
                        note = dbWorkout.plan.note,
                        entries = emptyList(),
                        daysOfWeek = dbWorkout.plan.daysOfWeek.map { DayOfWeek.valueOf(it) }
                ),
                entries = entryData.map { entry ->
                    WorkoutEntry(
                        position = entry.position.position,
                        exercise = entry.exercise?.toExercise(),
                        expectedSet = dbWorkout.goals.find { it.goal.positionId == entry.position.epId }?.goal
                            ?.toExpectedSet(entry.position.position),
                        sets = if (entry.exercise == null) emptyList()
                            else entry.exerciseSets.map { set ->
                                set.toExerciseSet(entry.exercise.toExercise())
                            }.sortedBy { it.setNumber }
                    )
                }
            )
        }
    }

    override fun getPagedWorkouts(): Flow<PagingData<Workout>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = {
                database.workoutDao().getAllPaged()
            }
        ).flow.map { pagingData ->
            pagingData.map { data ->
                data.workout.toWorkout().copy(
                    entries = data.sets.groupBy { it.positionInWorkout.position }.map { set ->
                        WorkoutEntry(
                            position = set.key,
                            exercise = set.value.first().exercise.toExercise(),
                            expectedSet = null,
                            sets = set.value.map { it.set.toExerciseSet(it.exercise.toExercise()) }.sortedBy { it.setNumber }
                        )
                    }.sortedBy { it.position },
                ) }
        }
    }

    override suspend fun getWorkoutCompletedDates(monthsBack: Int): List<OffsetDateTime> {
        val earliest = OffsetDateTime.now().minusMonths(monthsBack.toLong())
        return database.workoutDao().getAllCompletedWorkoutDates(earliest.toUTCEpochMilli())
    }

    override suspend fun getWorkout(id: Long): Workout {
        Timber.d("*** getting workout $id")
        val dbWorkout = database.workoutDao().getWorkout(id)
        val sets = database.exercisePositionDao().getPositionsWithExerciseAndSets(dbWorkout.wId)

        val plan = when (dbWorkout.planId) {
            null -> null
            else -> database.workoutPlanDao().getWorkoutPlanWithGoalsById(dbWorkout.planId)
        }

        return Workout(
            id = dbWorkout.wId,
            name = dbWorkout.name,
            note = dbWorkout.note,
            completedAt = dbWorkout.completedAt,
            addedAt = dbWorkout.addedAt,
            entries = sets.map { data ->
                val matchingGoal = plan?.goals?.find { goal ->
                    goal.exerciseGroupId == data.position.groupId ||
                            goal.exerciseId == data.position.exerciseId
                }
                val groupedExercises = when {
                    matchingGoal?.exerciseGroupId != null -> database.exerciseGroupDao()
                        .getGroupByIdWithExercises(matchingGoal.exerciseGroupId).exercises
                    else -> emptyList()
                }

                WorkoutEntry(
                    position = data.position.position,
                    exercise = data.exercise?.toExercise(),
                    expectedSet = matchingGoal?.toExpectedSet(
                        data.position.position,
                        data.exercise?.toExercise(),
                        data.group?.toExerciseGroup(groupedExercises.map { it.toExercise() })
                    ),
                    sets = data.exerciseSets.mapNotNull { set ->
                        if (data.exercise == null) null
                        else set.toExerciseSet(data.exercise.toExercise())
                    }.sortedBy { it.setNumber }
                )
            }.sortedBy { it.position },
            plan = if (plan == null) null else
                WorkoutPlan(
                    id = plan.plan.wpId,
                    addedAt = plan.plan.addedAt,
                    name = plan.plan.name,
                    note = plan.plan.note,
                    entries = emptyList(),
                    daysOfWeek = plan.plan.daysOfWeek.map { DayOfWeek.valueOf(it) }
                )
        )
    }

    override suspend fun createWorkout(workout: Workout, planId: Long?): Workout {
        val planEntry = when (planId) {
            null -> null
            else -> database.workoutPlanDao().getWorkoutPlanWithGoalsById(planId)
        }
        val plan = planEntry?.plan

        val workoutId = database.workoutDao().insert(
            WorkoutEntity(
                wId = 0L,
                planId = plan?.wpId,
                name = plan?.name ?: "${OffsetDateTime.now().dayOfWeek.name.lowercase()} workout",
                note = plan?.note,
                minutesToComplete = 0,
                completedAt = workout.completedAt,
                addedAt = workout.addedAt,
            )
        )

        var createdWorkout = workout.copy(id = workoutId)
        if (planEntry != null && plan != null) {
            val planPositions = database.exercisePositionDao().getPositionsInPlan(plan.wpId)
            val dbExercises = database.exerciseDao().getExercisesByIds(planEntry.goals.mapNotNull { it.exerciseId })

            createdWorkout = createdWorkout.copy(
                plan = WorkoutPlan(
                    id = plan.wpId,
                    addedAt = plan.addedAt,
                    name = plan.name,
                    note = plan.note,
                    entries = emptyList(),
                    daysOfWeek = plan.daysOfWeek.map { DayOfWeek.valueOf(it) }
                ),
            )

            val positions = planPositions
                .map { position ->
                    ExercisePositionEntity(
                        epId = 0L,
                        exerciseId = position.exerciseId,
                        groupId = position.groupId,
                        workoutId = workoutId,
                        position = position.position,
                    )
                }
            database.exercisePositionDao().insertAll(positions)

            val addedPositions = database.exercisePositionDao().getPositionsInWorkout(workoutId)
            val individualSets = planEntry.goals
                .filter { it.exerciseId != null }
                .flatMap { goal ->
                    val tmp = arrayListOf<SetEntity>()
                    val position = addedPositions.first { goalPosition ->
                        goalPosition.groupId == goal.exerciseGroupId ||
                                goalPosition.exerciseId == goal.exerciseId
                    }

                    for (i in 0 until goal.sets) {
                        val lastSet = database.exerciseSetDao().getLastCompletedSet(goal.exerciseId!!)

                        tmp.add(SetEntity(
                            sId = 0L,
                            exerciseId = goal.exerciseId,
                            workoutId = workoutId,
                            positionId = position.epId,
                            reps = goal.reps.takeIf { it > 0 } ?: lastSet?.reps ?: 0,
                            weightInPounds = goal.weightInPounds.takeIf { it > 0 } ?: lastSet?.weightInPounds ?: 0f,
                            weightInKilograms = goal.weightInKilograms.takeIf { it > 0 } ?: lastSet?.weightInKilograms ?: 0f,
                            repsInReserve = goal.repsInReserve.takeIf { it > 0 } ?: lastSet?.repsInReserve ?: 0,
                            perceivedExertion = goal.perceivedExertion.takeIf { it > 0 } ?: lastSet?.perceivedExertion ?: 0,
                            setNumber = (i + 1),
                            seconds = 0,
                            modifier = goal.modifier,
                            type = goal.type,
                        ))
                    }
                    tmp
                }

            database.exerciseSetDao().insertAll(individualSets)

            val insertedSets = database.exerciseSetDao().getSetsInWorkout(workoutId)
            createdWorkout = createdWorkout.copy(
                entries = addedPositions.map { position ->
                    val matchingExercise = dbExercises.find { it.eId == position.exerciseId }
                    val matchingGoal = planEntry.goals.find { goal ->
                        goal.exerciseId == position.exerciseId ||
                                goal.exerciseGroupId == position.groupId
                    }
                    val goalExercise = dbExercises.firstOrNull { matchingGoal != null && it.eId == matchingGoal.exerciseId }
                    val goalGroup = position.groupId?.let { database.exerciseGroupDao().getGroupByIdWithExercises(it) }

                    WorkoutEntry(
                        position = position.position,
                        exercise = matchingExercise?.toExercise(),
                        expectedSet = matchingGoal?.toExpectedSet(
                            position.position,
                            exercise = goalExercise?.toExercise(),
                            group = goalGroup?.group?.toExerciseGroup(
                                goalGroup.exercises.map { it.toExercise() }.orEmpty())
                        ),
                        sets = insertedSets.filter { it.exerciseId == position.exerciseId }
                            .map { item ->
                                item.toExerciseSet(matchingExercise!!.toExercise())
                            }
                            .sortedBy { it.setNumber }
                    )
                }.sortedBy { it.position },
            )
        }

        return createdWorkout
    }

    override suspend fun deleteWorkout(workout: Workout) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workout.addedAt)
        database.workoutDao().delete(dbWorkout)
    }

    override suspend fun updateWorkout(workout: Workout): Workout {
        val existingWorkout = database.workoutDao().getWorkout(workout.id)

        database.workoutDao().update(workout.toDbWorkout(
            existingWorkout.wId,
            planId = existingWorkout.planId
        ))
        return workout
    }

    override suspend fun addEntry(
        workoutEntry: WorkoutEntry,
        workoutAddedAt: OffsetDateTime
    ): WorkoutEntry {
        val workoutEntity = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val positions = database.exercisePositionDao().getPositionsInWorkout(workoutEntity.wId)

        val exercise = when (workoutEntry.exercise) {
            null -> null
            else -> database.exerciseDao().getExerciseByName(workoutEntry.exercise!!.name)
        }

        var insertedSet = null as SetEntity?

        if (exercise != null) {
            val insertedPositionId = database.exercisePositionDao().insert(
                ExercisePositionEntity(
                    epId = 0L,
                    workoutId = workoutEntity.wId,
                    position = positions.size + 1,
                    exerciseId = exercise.eId,
                )
            )

            val lastSet = database.exerciseSetDao().getLastCompletedSet(exercise.eId)
            insertedSet = lastSet?.copy(
                    sId = 0L,
                    exerciseId = exercise.eId,
                    workoutId = workoutEntity.wId,
                    positionId = insertedPositionId,
                    setNumber = 1,
                    completedAt = null,
                ) ?:
                SetEntity(
                    sId = 0L,
                    exerciseId = exercise.eId,
                    workoutId = workoutEntity.wId,
                    positionId = insertedPositionId,
                    setNumber = 1,
                    completedAt = null,
                )
            database.exerciseSetDao().insert(insertedSet)
        }


        return WorkoutEntry(
            position = positions.size + 1,
            exercise = exercise?.toExercise(),
            sets = listOfNotNull(exercise?.toExercise()?.let { insertedSet?.toExerciseSet(it) })
        )
    }

    override suspend fun removeEntryFromWorkout(entry: WorkoutEntry, workoutAddedAt: OffsetDateTime) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val positions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)

        // delete position of exercise to remove
        positions.find { it.position == entry.position }?.let { pos ->
            database.exercisePositionDao().delete(pos)
            // ExerciseSets should be deleted because of CASCADE
        }

        // update other positions
        val positionsToMove = positions.filter { pos ->
            pos.position >= entry.position
        }
        database.exercisePositionDao().updateAll(
            positionsToMove.map { it.copy(position = it.position - 1) }
        )
    }

    override suspend fun updateCompletedSet(workout: Workout, exerciseSet: ExerciseSet) {
        database.exerciseSetDao().updatePartial(exerciseSet = ExerciseSetPartial(
            exerciseSet.id,
            reps = exerciseSet.reps,
            setNumber = exerciseSet.setNumber,
            weightInPounds = exerciseSet.weightInPounds,
            weightInKilograms = exerciseSet.weightInKilograms,
            repsInReserve = exerciseSet.repsInReserve,
            perceivedExertion = exerciseSet.perceivedExertion,
        ))
    }

    override suspend fun deleteSet(set: ExerciseSet, workoutId: Long) {
        val setEntity = database.exerciseSetDao().getSet(set.id)
        database.exerciseSetDao().delete(set.id)

        val positions = database.exercisePositionDao().getPositionsWithExerciseAndSets(workoutId)
        val position = positions.find { it.position.epId == setEntity.positionId }
        if (position?.exerciseSets?.isEmpty() == true) {
            database.exercisePositionDao().delete(position.position)
        }
    }

    override suspend fun swapEntryPosition(
        workoutAddedAt: OffsetDateTime,
        entry: WorkoutEntry,
        newPosition: Int
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbPositions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)

        dbPositions.find { it.position == entry.position }?.let { entryToMove ->
            val movingUp = entryToMove.position > newPosition
            dbPositions.find { it.position == newPosition }?.let { positionInWantedSpot ->
                database.exercisePositionDao().update(
                    positionInWantedSpot.copy(
                        position = if (movingUp)
                            (positionInWantedSpot.position + 1)
                        else
                            (positionInWantedSpot.position - 1)
                    )
                )
            }

            database.exercisePositionDao().update(entryToMove.copy(position = newPosition))
        }
    }

    override suspend fun replaceGroupWithExercise(
        workoutAddedAt: OffsetDateTime,
        group: ExerciseGroup,
        exercise: Exercise,
        position: Int,
        expectedSet: ExpectedSet?
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbPositions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)


        dbPositions.firstOrNull { it.position == position }?.let { pos ->
            Timber.d("*** adding ${dbExercise?.name} exercise from group ${group.name}")
            database.exercisePositionDao().update(pos.copy(
                exerciseId = dbExercise?.eId,
            ))

            if (dbExercise != null) {
                Timber.d("*** expectedSet = $expectedSet")
                if (expectedSet != null) {
                    val minSets = if (expectedSet.sets == 0) 1 else expectedSet.sets
                    val inserted = database.exerciseSetDao().insertAll(
                        IntRange(1, minSets).toList().map { number ->
                            SetEntity(
                                sId = 0L,
                                exerciseId = dbExercise.eId,
                                workoutId = dbWorkout.wId,
                                groupId = group.id,
                                positionId = pos.epId,
                                reps = expectedSet.reps,
                                repsInReserve = expectedSet.rir,
                                perceivedExertion = expectedSet.perceivedExertion,
                                type = expectedSet.type,
                                setNumber = number,
                            )
                        }
                    )
                    Timber.d("*** added ${inserted.size} sets")
                } else {
                    database.exerciseSetDao().insert(
                        SetEntity(
                            sId = 0L,
                            exerciseId = dbExercise.eId,
                            workoutId = dbWorkout.wId,
                            groupId = group.id,
                            positionId = pos.epId
                        )
                    )
                }
            }
        }
    }

    override suspend fun replaceExerciseWithGroup(
        workoutAddedAt: OffsetDateTime,
        entry: WorkoutEntry,
    ): WorkoutEntry {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbPositions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)

        dbPositions.firstOrNull { it.position == entry.position }?.let { pos ->
            database.exercisePositionDao().update(pos.copy(
                exerciseId = null,
            ))

            val dbSets = database.exerciseSetDao().getSetsInWorkout(dbWorkout.wId)
            database.exerciseSetDao().deleteAll(dbSets.filter { it.positionId == pos.epId })
        }

        return WorkoutEntry(
            position = entry.position,
            exercise = null,
            expectedSet = entry.expectedSet,
            sets = emptyList()
        )
    }
}