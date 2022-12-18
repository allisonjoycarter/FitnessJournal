package com.catscoffeeandkitchen.data.workouts.repository

import androidx.paging.*
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSetPartial
import com.catscoffeeandkitchen.data.workouts.models.SetEntity
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity
import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
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
    private val exerciseSearchService: ExerciseSearchService.Impl
): WorkoutRepository {
    override suspend fun getWorkouts(): List<Workout> {
        return database.workoutDao().getWorkoutsWithPlans().map { dbWorkout ->
            val entryData = database.exercisePositionDao()
                .getPositionsWithExerciseAndSets(dbWorkout.workout.wId)

            Workout(
                name = dbWorkout.workout.name,
                note = dbWorkout.workout.note,
                completedAt = dbWorkout.workout.completedAt,
                addedAt = dbWorkout.workout.addedAt,
                plan = if (dbWorkout.plan == null) null else
                    WorkoutPlan(
                    addedAt = dbWorkout.plan.addedAt,
                    name = dbWorkout.plan.name,
                    note = dbWorkout.plan.note,
                    entries = emptyList()
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

    override suspend fun getCompletedWorkouts(): List<Workout> {
        return database.workoutDao().getAllCompletedWorkouts().map { dbWorkout ->
            Workout(
                completedAt = dbWorkout.completedAt,
                addedAt = dbWorkout.addedAt,
            )
        }
    }

    override suspend fun getWorkoutByAddedDate(addedAt: OffsetDateTime): Workout {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(addedAt)
        val sets = database.exercisePositionDao()
            .getPositionsWithExerciseAndSets(dbWorkout.wId)

        val plan = when (dbWorkout.planId) {
            null -> null
            else -> database.workoutPlanDao().getWorkoutPlanWithGoalsById(dbWorkout.planId)
        }

        return Workout(
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
                    addedAt = plan.plan.addedAt,
                    name = plan.plan.name,
                    note = plan.plan.note,
                    entries = emptyList(),
                )
        )
    }

    override suspend fun createWorkout(workout: Workout, planAddedAt: OffsetDateTime?): Workout {
        val dbPlan = when (planAddedAt) {
            null -> null
            else -> database.workoutPlanDao().getWithGoalsByAddedAt(planAddedAt)
        }
        val workoutId = database.workoutDao().insert(
            WorkoutEntity(
                wId = 0L,
                planId = dbPlan?.plan?.wpId,
                name = dbPlan?.plan?.name ?: "${OffsetDateTime.now().dayOfWeek.name.lowercase()} workout",
                note = dbPlan?.plan?.note,
                minutesToComplete = 0,
                completedAt = workout.completedAt,
                addedAt = workout.addedAt,
            )
        )

        var createdWorkout = workout
        if (dbPlan != null) {
            val planPositions = database.exercisePositionDao().getPositionsInPlan(dbPlan.plan.wpId)
            val dbExercises = database.exerciseDao().getExercisesByIds(dbPlan.goals.mapNotNull { it.exerciseId })

            createdWorkout = createdWorkout.copy(
                plan = WorkoutPlan(
                    addedAt = dbPlan.plan.addedAt,
                    name = dbPlan.plan.name,
                    note = dbPlan.plan.note,
                    entries = emptyList(),
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
            val individualSets = dbPlan.goals
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
                entries = positions.map { position ->
                    val matchingExercise = dbExercises.find { it.eId == position.exerciseId }
                    val matchingGoal = dbPlan.goals.find { goal ->
                        goal.positionId == planPositions.find { it.position == position.position }?.epId
                    }

                    WorkoutEntry(
                        position = position.position,
                        exercise = matchingExercise?.toExercise(),
                        expectedSet = matchingGoal?.toExpectedSet(position.position),
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
        val existingWorkout = database.workoutDao().getWorkoutByAddedAt(workout.addedAt)

        database.workoutDao().update(workout.toDbWorkout(
            existingWorkout.wId,
            planId = existingWorkout.planId
        ))
        return workout
    }

    override suspend fun getExercises(names: List<String>?): List<Exercise> {
        if (names != null) {
            return database.exerciseDao().getExercisesByName(names).map { exercise ->
                exercise.toExercise()
            }
        }
        return database.exerciseDao().getAllExercises().map { exerciseWithCount ->
            exerciseWithCount.exercise.toExercise(amountOfSets = exerciseWithCount.amountPerformed)
        }
    }

    override fun getPagedExercises(search: String?, muscle: String?, category: String?): Flow<PagingData<Exercise>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            remoteMediator = ExerciseRemoteMediator(
                search.orEmpty(),
                muscle.orEmpty(),
                category.orEmpty(),
                exerciseSearchService,
                database
            ),
            pagingSourceFactory = {
                if (search.isNullOrEmpty() && muscle.isNullOrEmpty() && category.isNullOrEmpty()) {
                    database.exerciseDao().getAllPagedExercises()
                } else {
                    database.exerciseDao().getPagedExercisesByName(
                        search.orEmpty(),
                        muscle.orEmpty(),
                        category.orEmpty()
                    )
                }
            }
        ).flow.map { pagingData ->
            pagingData.map { it.exercise.toExercise() }
        }
    }

    override suspend fun createExercise(exercise: Exercise): Exercise {
        database.exerciseDao().insert(exercise = exercise.toDbExercise(id = 0L, userCreated = true))
        return exercise
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
                ) ?:
                SetEntity(
                    sId = 0L,
                    exerciseId = exercise.eId,
                    workoutId = workoutEntity.wId,
                    positionId = insertedPositionId,
                    setNumber = 1,
                )
            database.exerciseSetDao().insert(insertedSet)
        }


        return WorkoutEntry(
            position = positions.size + 1,
            exercise = exercise?.toExercise(),
            sets = listOfNotNull(exercise?.toExercise()?.let { insertedSet?.toExerciseSet(it) })
        )
    }

    override suspend fun updateExercise(exercise: Exercise, workout: Workout?) {
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)
        dbExercise?.let { dbEx ->
            database.exerciseDao().update(exercise.toDbExercise(id = dbEx.eId))
        }
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

    override suspend fun deleteSet(setId: Long) {
        database.exerciseSetDao().delete(setId)
    }

    override suspend fun getExerciseByName(name: String): Exercise? {
        val dbExercise = database.exerciseDao().getExerciseByName(name) ?: return null
        return dbExercise.toExercise()
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