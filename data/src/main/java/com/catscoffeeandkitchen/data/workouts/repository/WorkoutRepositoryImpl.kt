package com.catscoffeeandkitchen.data.workouts.repository

import android.icu.text.DateFormat.HourCycle
import androidx.paging.*
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionInWorkout
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSetPartial
import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.util.*
import com.catscoffeeandkitchen.data.workouts.models.SetEntity as DbExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity as DbWorkout
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneId

@OptIn(ExperimentalPagingApi::class)
class WorkoutRepositoryImpl(
    val database: FitnessJournalDb,
    private val exerciseSearchService: ExerciseSearchService.Impl
): WorkoutRepository {
    override suspend fun getWorkouts(): List<Workout> {
        return database.workoutDao().getWorkoutsWithPlans().map { dbWorkout ->
            val refs = database.exercisePositionDao()
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
                    exercises = dbWorkout.goals.map { it.toExpectedSet(it.exercise.toExercise(it.goal.positionInWorkout)) },
                ),
                sets = refs.filter { it.exercise != null }.flatMap { combined ->
                    combined.exerciseSets.map { it.toExerciseSet(
                        combined.exercise!!.toExercise(combined.position.position))
                    }
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
                    sets = data.sets.map { setData ->
                        setData.set.toExerciseSet(
                            setData.exercise.toExercise(setData.positionInWorkout.position))
                    }.sortedBy { it.exercise.positionInWorkout }
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
        Timber.d("*** retrieving ${sets.joinToString { "${it.exercise?.name}${it.position.position}" }}")

        return Workout(
            name = dbWorkout.name,
            note = dbWorkout.note,
            completedAt = dbWorkout.completedAt,
            addedAt = dbWorkout.addedAt,
            sets = sets.flatMap { combined -> combined.exerciseSets.mapNotNull { set ->
                if (combined.exercise == null) {
                    null
                } else {
                    set.toExerciseSet(
                        exercise = combined.exercise.toExercise(combined.position.position)
                    )
                }
            }},
            plan = if (plan == null) null else
                WorkoutPlan(
                    addedAt = plan.plan.addedAt,
                    name = plan.plan.name,
                    note = plan.plan.note,
                    exercises = plan.goals.map { goal ->
                        val exercise = goal.exerciseId?.let { database.exerciseDao().getExerciseById(it) }
                        val group = goal.exerciseGroupId?.let { database.exerciseGroupDao().getGroupByIdWithExercises(it) }

                        goal.toExpectedSet(
                            exercise = exercise?.toExercise(goal.positionInWorkout),
                            group = group?.group?.toExerciseGroup(group.exercises.map { entry ->
                                val relatedStats = database.exerciseDao().getExerciseWithStatsByName(
                                    entry.name,
                                    startOfWeek = OffsetDateTime.now().inUTC()
                                        .with(DayOfWeek.MONDAY).withHour(0).toUTCEpochMilli(),
                                    currentTime = OffsetDateTime.now().toUTCEpochMilli()
                                )
                                entry.toExercise(
                                    position = goal.positionInWorkout,
                                    stats = relatedStats?.toStats()
                                )
                            })
                        )
                    },
                )
        )
    }

    override suspend fun createWorkout(workout: Workout, planAddedAt: OffsetDateTime?): Workout {
        val dbPlan = when (planAddedAt) {
            null -> null
            else -> database.workoutPlanDao().getWorkoutPlanWithGoalsByAddedAt(planAddedAt)
        }
        val workoutId = database.workoutDao().insert(DbWorkout(
            wId = 0L,
            planId = dbPlan?.plan?.wpId,
            name = dbPlan?.plan?.name ?: "${OffsetDateTime.now().dayOfWeek.name.lowercase()} workout",
            note = dbPlan?.plan?.note,
            minutesToComplete = 0,
            completedAt = workout.completedAt,
            addedAt = workout.addedAt,
        ))

        var createdWorkout = workout
        if (dbPlan != null) {
            val dbExercises = database.exerciseDao().getExercisesByIds(dbPlan.goals.mapNotNull { it.exerciseId })
            createdWorkout = createdWorkout.copy(
                plan = WorkoutPlan(
                    addedAt = dbPlan.plan.addedAt,
                    name = dbPlan.plan.name,
                    note = dbPlan.plan.note,
                    exercises = dbPlan.goals.map { goal ->
                        val relatedExercise = dbExercises.find { it.eId == goal.exerciseId }
                        val relatedGroup = goal.exerciseGroupId?.let { id ->
                            database.exerciseGroupDao().getGroupByIdWithExercises(id)
                        }

                        goal.toExpectedSet(
                            exercise = relatedExercise?.toExercise(goal.positionInWorkout),
                            group = relatedGroup?.group?.toExerciseGroup(
                                relatedGroup.exercises.map { entry ->
                                    val relatedStats = database.exerciseDao().getExerciseWithStatsByName(
                                        relatedExercise?.name.orEmpty(),
                                        startOfWeek = OffsetDateTime.now()
                                            .inUTC()
                                            .with(DayOfWeek.MONDAY).withHour(0)
                                            .toUTCEpochMilli(),
                                        currentTime = OffsetDateTime.now().toUTCEpochMilli()
                                    )
                                    entry.toExercise(
                                        position = goal.positionInWorkout,
                                        stats = relatedStats?.toStats()
                                    ) }
                            )
                        )
                    }
                ),
            )

            var positionNumber = 0
            val positions = dbPlan.goals
                .sortedBy { it.positionInWorkout }
                .map { goal ->
                    positionNumber++
                    ExercisePositionInWorkout(
                        epId = 0L,
                        exerciseId = goal.exerciseId,
                        groupId = goal.exerciseGroupId,
                        workoutId = workoutId,
                        position = positionNumber,
                    )
                }

            val positionIds = database.exercisePositionDao().insertAll(positions)

            val individualSets = dbPlan.goals
                .sortedBy { it.positionInWorkout }
                .filter { it.exerciseId != null }
                .flatMapIndexed { index, goal ->
                    val tmp = arrayListOf<DbExerciseSet>()

                    for (i in 0 until goal.sets) {
                        val lastSet = database.exerciseSetDao().getLastCompletedSet(goal.exerciseId!!)

                        tmp.add(DbExerciseSet(
                            sId = 0L,
                            exerciseId = goal.exerciseId,
                            workoutId = workoutId,
                            positionId = positionIds[index],
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

            Timber.d("adding to workout ${individualSets.joinToString(", ") { "exercise ${it.exerciseId} (${it.setNumber})" }})")
            database.exerciseSetDao().insertAll(individualSets)

            val insertedSets = database.exerciseSetDao().getSetsInWorkout(workoutId)
            createdWorkout = createdWorkout.copy(sets = insertedSets.map { set ->
                val matchingExercise = dbExercises.find { it.eId == set.exerciseId }
                val matchingPosition = positions.find { it.exerciseId == set.exerciseId }
                set.toExerciseSet(matchingExercise!!.toExercise(matchingPosition?.position))
            })
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

        workout.sets.forEach { set ->
            val dbExercise = database.exerciseDao().getExerciseByName(set.exercise.name)
            var exerciseId = dbExercise?.eId
            if (dbExercise != null) {
                database.exerciseDao().update(set.exercise.toDbExercise(dbExercise.eId))
            } else {
                exerciseId = database.exerciseDao().insert(set.exercise.toDbExercise())
            }

            val position = database.exercisePositionDao().getPositionsInWorkoutWithExerciseId(
                existingWorkout.wId,
                exerciseId!!
            )

            database.exerciseSetDao().update(
                set.toDbExerciseSet(exerciseId, existingWorkout.wId, position.first().epId)
            )
        }
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
        Timber.d("getPagedExercises in repository, search = $search , muscle = $muscle, category = $category")
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

    override suspend fun updateExercise(exercise: Exercise, workout: Workout?) {
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)
        dbExercise?.let { dbEx ->
            database.exerciseDao().update(exercise.toDbExercise(id = dbEx.eId))
        }
    }

    override suspend fun removeExerciseFromWorkout(exercise: Exercise, workoutAddedAt: OffsetDateTime) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val positions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)

        // delete position of exercise to remove
        positions.find { it.position == exercise.positionInWorkout }?.let { pos ->
            database.exercisePositionDao().delete(pos)
            // ExerciseSets should be deleted because of CASCADE
        }

        // update other positions
        val positionsToMove = positions.filter { pos ->
            pos.position >= (exercise.positionInWorkout ?: 100)
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

    override suspend fun swapExercisePosition(
        workoutAddedAt: OffsetDateTime,
        exercise: Exercise,
        newPosition: Int
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbPositions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)

        dbPositions.find { it.position == exercise.positionInWorkout }?.let { pos ->
            val movingUp = pos.position > newPosition
            dbPositions.find { it.position == newPosition }?.let { positionToMove ->
                database.exercisePositionDao().update(
                    positionToMove.copy(
                        position = if (movingUp)
                            (positionToMove.position + 1)
                        else
                            (positionToMove.position - 1)
                    )
                )
            }

            database.exercisePositionDao().update(pos.copy(position = newPosition))
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

        Timber.d("*** adding ${dbExercise?.name} exercise from group ${group.name}")

        dbPositions.firstOrNull { it.position == position }?.let { pos ->
            database.exercisePositionDao().update(pos.copy(
                exerciseId = dbExercise?.eId,
            ))

            if (dbExercise != null) {
                Timber.d("*** expectedSet = $expectedSet")
                if (expectedSet != null) {
                    val minSets = if (expectedSet.sets == 0) 1 else expectedSet.sets
                    database.exerciseSetDao().insertAll(
                        IntRange(1, minSets).toList().map { number ->
                            DbExerciseSet(
                                sId = 0L,
                                exerciseId = dbExercise.eId,
                                workoutId = dbWorkout.wId,
                                positionId = pos.epId,
                                reps = expectedSet.reps,
                                repsInReserve = expectedSet.rir,
                                perceivedExertion = expectedSet.perceivedExertion,
                                type = expectedSet.type,
                                setNumber = number
                            )
                        }
                    )
                } else {
                    database.exerciseSetDao().insert(DbExerciseSet(
                        sId = 0L,
                        exerciseId = dbExercise.eId,
                        workoutId = dbWorkout.wId,
                        positionId = pos.epId
                    ))
                }
            }
        }
    }

    override suspend fun replaceExerciseWithGroup(
        workoutAddedAt: OffsetDateTime,
        exercise: Exercise,
        position: Int
    ) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workoutAddedAt)
        val dbPositions = database.exercisePositionDao().getPositionsInWorkout(dbWorkout.wId)

        dbPositions.firstOrNull { it.position == position }?.let { pos ->
            database.exercisePositionDao().update(pos.copy(
                exerciseId = null,
            ))

            val dbSets = database.exerciseSetDao().getSetsInWorkout(dbWorkout.wId)
            database.exerciseSetDao().deleteAll(dbSets)
        }
    }
}