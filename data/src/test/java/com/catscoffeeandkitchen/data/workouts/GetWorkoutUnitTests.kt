package com.catscoffeeandkitchen.data.workouts

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.catscoffeeandkitchen.data.TestDataModule
import com.catscoffeeandkitchen.data.TestExerciseSearchService
import com.catscoffeeandkitchen.data.workouts.db.*
import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.data.workouts.repository.WorkoutRepositoryImpl
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Workout
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.OffsetDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GetWorkoutUnitTests {
    private val testScope = TestScope()
    private val testDispatcher = StandardTestDispatcher(testScope.testScheduler)

    private lateinit var db: FitnessJournalDb
    private lateinit var workoutDao: WorkoutDao
    private lateinit var planDao: WorkoutPlanDao
    private lateinit var positionsDao: ExercisePositionDao
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var exerciseGoalDao: ExerciseGoalDao
    private lateinit var groupDao: ExerciseGroupDao
    private lateinit var setDao: ExerciseSetDao
    private lateinit var workoutRepository: WorkoutRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()

        db = TestDataModule().provideAppDatabase(context)
        workoutDao = db.workoutDao()
        planDao = db.workoutPlanDao()
        exerciseDao = db.exerciseDao()
        exerciseGoalDao = db.exerciseGoalDao()
        positionsDao = db.exercisePositionDao()
        groupDao = db.exerciseGroupDao()
        setDao = db.exerciseSetDao()

        workoutRepository = WorkoutRepositoryImpl(db)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun getWorkoutsTest() = runTest {
        val amountOfWorkouts = 10
        for (i in 0 until amountOfWorkouts) {
            workoutDao.insert(
                WorkoutEntity(
                    0L,
                    name = "test$i"
                )
            )
        }

        val allWorkouts = workoutRepository.getWorkouts()
        assertEquals(amountOfWorkouts, allWorkouts.size)
        for (i in 0 until amountOfWorkouts) {
            assert(allWorkouts.any { it.name == "test$i" })
        }
    }

    @Test
    fun getWorkoutsWithPlansTest() = runTest {
        val amountOfWorkouts = 10
        for (i in 0 until amountOfWorkouts) {
            val planId = when (i < (amountOfWorkouts / 2)) {
                true -> planDao.insert(
                    WorkoutPlanEntity(
                        0L,
                        name = "test_plan"
                    )
                )
                else -> null
            }

            workoutDao.insert(
                WorkoutEntity(
                    0L,
                    name = "test$i",
                    planId = planId
                )
            )
        }

        val allWorkouts = workoutRepository.getWorkouts()
        assertEquals(amountOfWorkouts, allWorkouts.size)
        for (i in 0 until amountOfWorkouts) {
            assert(allWorkouts.any { it.name == "test$i" })
        }

        assertEquals(amountOfWorkouts / 2, allWorkouts.filter { it.plan != null }.size)
        assertEquals("test_plan", allWorkouts.first { it.plan != null }.plan?.name)
    }

    @Test
    fun getWorkoutsWithExercisesTest() = runTest {
        val exerciseId = exerciseDao.insert(
            ExerciseEntity(0L, name = "Bicep Curl")
        )

        val amountOfWorkouts = 5
        for (i in 0 until amountOfWorkouts) {
            val workoutId = workoutDao.insert(
                WorkoutEntity(
                    0L,
                    name = "test$i",
                )
            )

            val groupId = groupDao.insert(
                ExerciseGroupEntity(0L, name = "test_group")
            )
            groupDao.insertAllRefs(listOf(GroupExerciseXRef(0L, exerciseId, groupId)))

            val planId = planDao.insert(
                    WorkoutPlanEntity(
                        0L,
                        name = "test_plan"
                    )
            )
            val groupPositionId = positionsDao.insert(
                ExercisePositionEntity(0L, planId = planId, groupId = groupId, position = 2)
            )
            exerciseGoalDao.insert(
                ExerciseGoal(
                    0L,
                    workoutPlanId = planId,
                    exerciseGroupId = groupId,
                    positionId = groupPositionId,
                    reps = 10, sets = 4
                )
            )

            val position = positionsDao.insert(
                ExercisePositionEntity(
                    0L,
                    exerciseId = exerciseId,
                    workoutId = workoutId,
                )
            )

            setDao.insertAll(listOf(0, 1, 2).map {
                SetEntity(
                    0L,
                    exerciseId = exerciseId,
                    positionId = position,
                    reps = 5,
                    workoutId = workoutId
                )
            })
        }

        val allWorkouts = workoutRepository.getWorkouts()
        assertEquals(amountOfWorkouts, allWorkouts.size)
        assertEquals(3, allWorkouts.first().entries.first().sets.size)
    }

    @Test
    fun getPagedWorkoutsTest() = testScope.runTest {
        val amountOfWorkouts = 10
        for (i in 0 until amountOfWorkouts) {
            workoutDao.insert(
                WorkoutEntity(
                    0L,
                    name = "test$i"
                )
            )
        }

        // TODO: Figure out how to test this
//        workoutRepository.getPagedWorkouts().take(1).collect { data ->
//            val differ = AsyncPagingDataDiffer(
//                diffCallback = MyDiffCallback(),
//                updateCallback = NoopListCallback(),
//                workerDispatcher = Dispatchers.Main
//            )
//
//            differ.submitData(data)
//            advanceUntilIdle()
//            assertEquals(amountOfWorkouts, differ.snapshot().size)
//        }
    }

    @Test
    fun getCompletedWorkoutDatesTest() = runTest {
        val dates = listOf(
            OffsetDateTime.of(2022, 3, 1, 0, 0, 0, 0, ZoneOffset.of("Z")),
            OffsetDateTime.of(2022, 3, 10, 0, 0, 0, 0, ZoneOffset.of("Z")),
            OffsetDateTime.of(2022, 3, 20, 0, 0, 0, 0, ZoneOffset.of("Z")),
            OffsetDateTime.of(2022, 3, 30, 0, 0, 0, 0, ZoneOffset.of("Z")),
        )
        dates.forEach { date ->
            workoutDao.insert(
                WorkoutEntity(
                    0L,
                    name = "test${date.dayOfMonth}",
                    completedAt = date
                )
            )
        }

        val workoutDates = workoutRepository.getWorkoutCompletedDates(1000)
        assertEquals(dates.size, workoutDates.size)
        assertEquals(dates, workoutDates.sorted())
    }

    @Test
    fun getWorkoutByIdTest() = runTest {
        val id = 1L
        workoutDao.insert(
            WorkoutEntity(
                id,
                name = "test_workout",
            )
        )

        val workout = workoutRepository.getWorkout(id)

        assertEquals(id, workout.id)
        assertEquals("test_workout", workout.name)
    }

    @Test
    fun getWorkoutByIdWithPlanTest() = runTest {
        val id = 1L
        val planId = planDao.insert(
            WorkoutPlanEntity(
                0L,
                name = "test_plan"
            )
        )

        workoutDao.insert(
            WorkoutEntity(
                id,
                name = "test_workout",
                planId = planId,
            )
        )

        val exerciseId = exerciseDao.insert(ExerciseEntity(0L, name = "Bicep Curl"))
        val positionId = positionsDao.insert(
            ExercisePositionEntity(0L, exerciseId = exerciseId, workoutId = id, position = 1)
        )

        val groupId = groupDao.insert(
            ExerciseGroupEntity(0L, name = "test_group")
        )
        groupDao.insertAllRefs(listOf(GroupExerciseXRef(0L, exerciseId, groupId)))
        positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, groupId = groupId, workoutId = id, position = 2)
        )
        exerciseGoalDao.insert(
            ExerciseGoal(
                0L,
                workoutPlanId = planId,
                exerciseGroupId = groupId,
                positionId = positionId,
                reps = 10, sets = 4
            )
        )

        val workout = workoutRepository.getWorkout(id)

        assertEquals(id, workout.id)
        assertEquals("test_workout", workout.name)
        assertEquals("test_plan", workout.plan?.name)
        assertEquals(2, workout.entries.size)
        assertEquals(1, workout.entries.filter { it.expectedSet != null }.size)
        assertEquals(1, workout.entries.filter { it.exercise != null }.size)
    }
}