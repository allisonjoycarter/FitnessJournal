package com.catscoffeeandkitchen.data.workouts.repository

import androidx.paging.*
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSetPartial
import com.catscoffeeandkitchen.data.workouts.network.ExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.util.*
import com.catscoffeeandkitchen.data.workouts.models.ExerciseSet as DbExerciseSet
import com.catscoffeeandkitchen.data.workouts.models.Exercise as DbExercise
import com.catscoffeeandkitchen.data.workouts.models.Workout as DbWorkout
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.OffsetDateTime

@OptIn(ExperimentalPagingApi::class)
class WorkoutRepositoryImpl(
    val database: FitnessJournalDb,
    val exerciseSearchService: ExerciseSearchService.Impl
): WorkoutRepository {
    override suspend fun getWorkouts(): List<Workout> {
        return database.workoutDao().getWorkoutsWithPlans().map { dbWorkout ->
            val refs = database.exerciseDao().getSetsAndExercisesInWorkout(dbWorkout.workout.wId)
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
                    exercises = dbWorkout.goals.map { it.toExpectedSet(it.exercise.toExercise()) },
                ),
                sets = refs.map { it.toExerciseSet() }
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
            pagingData.map { it.toWorkout() }
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
        val sets = database.exerciseSetDao().getSetsAndExercisesInWorkout(dbWorkout.wId)

        val plan = when (dbWorkout.planId) {
            null -> null
            else -> database.workoutPlanDao().getWorkoutPlanWithGoalsById(dbWorkout.planId)
        }

        return Workout(
            name = dbWorkout.name,
            note = dbWorkout.note,
            completedAt = dbWorkout.completedAt,
            addedAt = dbWorkout.addedAt,
            sets = sets.map { it.set.toExerciseSet(it.exercise.toExercise()) },
            plan = if (plan == null) null else
                WorkoutPlan(
                    addedAt = plan.plan.addedAt,
                    name = plan.plan.name,
                    note = plan.plan.note,
                    exercises = plan.goals.map { goal ->
                        val exercise = database.exerciseDao().getExerciseById(goal.exerciseId)
                        goal.toExpectedSet(exercise.toExercise())
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
            val dbExercises = database.exerciseDao().getExercisesByIds(dbPlan.goals.map { it.exerciseId })
            createdWorkout = createdWorkout.copy(
                plan = WorkoutPlan(
                    addedAt = dbPlan.plan.addedAt,
                    name = dbPlan.plan.name,
                    note = dbPlan.plan.note,
                    exercises = dbPlan.goals.map { goal ->
                        val relatedExercise = dbExercises.find { it.eId == goal.exerciseId }
                        goal.toExpectedSet(relatedExercise!!.toExercise())
                    }
                ),
            )

            var setNumberInWorkout = 0
            val individualSets = dbPlan.goals
                .sortedBy { it.setNumberInWorkout }
                .flatMap { goal ->
                    val tmp = arrayListOf<DbExerciseSet>()
                    for (i in 0 until goal.sets) {
                        tmp.add(DbExerciseSet(
                            sId = 0L,
                            exerciseId = goal.exerciseId,
                            workoutId = workoutId,
                            reps = goal.reps,
                            weightInPounds = goal.weightInPounds,
                            weightInKilograms = goal.weightInKilograms,
                            repsInReserve = goal.repsInReserve,
                            perceivedExertion = goal.perceivedExertion,
                            setNumberInWorkout = setNumberInWorkout,
                            seconds = 0,
                            modifier = goal.modifier,
                            type = goal.type,
                        ))
                        setNumberInWorkout++
                    }
                    tmp
                }

            Timber.d("adding to workout ${individualSets.joinToString(", ") {it.setNumberInWorkout.toString()}})")
            database.exerciseSetDao().insertAll(individualSets)

            val insertedSets = database.exerciseSetDao().getSetsInWorkout(workoutId)
            createdWorkout = createdWorkout.copy(sets = insertedSets.map { set ->
                val matchingExercise = dbExercises.find { it.eId == set.exerciseId }
                set.toExerciseSet(matchingExercise!!.toExercise())
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

            database.exerciseSetDao().update(
                set.toDbExerciseSet(exerciseId!!, existingWorkout.wId)
            )
        }
        return workout
    }

    override suspend fun getExercises(): List<Exercise> {
        return database.exerciseDao().getAllExercises().map { it.toExercise() }
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

    override suspend fun removeExerciseFromWorkout(exercise: Exercise, workout: Workout) {
        val dbWorkout = database.workoutDao().getWorkoutByAddedAt(workout.addedAt)
        val sets = database.exerciseSetDao().getSetsInWorkout(dbWorkout.wId)
        val dbExercise = database.exerciseDao().getExerciseByName(exercise.name)
        val setNumber = sets.filter { it.exerciseId == dbExercise?.eId }.minOfOrNull { it.setNumberInWorkout }

        if (dbExercise != null && setNumber != null) {
            database.exerciseSetDao().deleteAll(sets.filter { dbExercise.eId == it.exerciseId })

            database.exerciseSetDao().updateAll(
                sets.filterNot { it.exerciseId == dbExercise.eId }
                    .filter { it.setNumberInWorkout > setNumber }
                    .mapIndexed { index, set -> set.copy(setNumberInWorkout = setNumber + index) }
            )
        }
    }

    override suspend fun updateCompletedSet(workout: Workout, exerciseSet: ExerciseSet) {
        database.exerciseSetDao().updatePartial(exerciseSet = ExerciseSetPartial(
            exerciseSet.id,
            reps = exerciseSet.reps,
            setNumberInWorkout = exerciseSet.setNumberInWorkout,
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
}