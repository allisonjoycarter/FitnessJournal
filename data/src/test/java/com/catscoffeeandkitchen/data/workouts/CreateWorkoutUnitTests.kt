package com.catscoffeeandkitchen.data.workouts

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.catscoffeeandkitchen.data.TestDataModule
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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CreateWorkoutUnitTests {
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
        val context = InstrumentationRegistry.getInstrumentation().context

        db = TestDataModule().provideAppDatabase(context)
        workoutDao = db.workoutDao()
        planDao = db.workoutPlanDao()
        exerciseDao = db.exerciseDao()
        exerciseGoalDao = db.exerciseGoalDao()
        positionsDao = db.exercisePositionDao()
        groupDao = db.exerciseGroupDao()
        setDao = db.exerciseSetDao()

        workoutRepository = WorkoutRepositoryImpl(db)

        Dispatchers.setMain(testDispatcher)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun createEmptyWorkoutTest() = runTest(testDispatcher) {
        val createdAt = OffsetDateTime.now()
        val workout = workoutRepository.createWorkout(
            Workout(
                id = 0L,
                addedAt = createdAt,
            ),
            planId = null,
        )

        val allWorkouts = workoutDao.getAll()
        assertEquals(1, allWorkouts.size)
        assertEquals(createdAt, workout.addedAt)
    }

    @Test
    fun createWorkoutBasedOnPlanTest() = runTest {
        val planId = planDao.insert(
            WorkoutPlanEntity(
                0L,
                name = "test_plan"
            )
        )

        val exerciseId = exerciseDao.insert(ExerciseEntity(0L, name = "Bicep Curl"))
        val positionId = positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, exerciseId = exerciseId)
        )
        exerciseGoalDao.insert(
            ExerciseGoal(
                0L,
                workoutPlanId = planId,
                exerciseId = exerciseId,
                positionId = positionId,
                reps = 10, sets = 4
            )
        )

        val workout = workoutRepository.createWorkout(
            Workout(
                id = 0L,
                addedAt = OffsetDateTime.parse("2022-03-01T00:00:00+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                name = "test_workout",
                plan = null
            ),
            planId = planId
        )

        assertEquals(planId, workout.plan?.id)
        assertEquals(1, workout.entries.size)
        assertEquals(4, workout.entries.first().expectedSet?.sets)
        assertEquals(4, workout.entries.first().sets.size)
        assertEquals("Bicep Curl", workout.entries.first().exercise?.name)
    }

    @Test
    fun createWorkoutBasedOnPlanWithPreviousSetDataTest() = runTest {
        val planId = planDao.insert(
            WorkoutPlanEntity(
                0L,
                name = "test_plan"
            )
        )

        val exerciseId = exerciseDao.insert(ExerciseEntity(0L, name = "Bicep Curl"))
        val positionId = positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, exerciseId = exerciseId)
        )
        exerciseGoalDao.insert(
            ExerciseGoal(
                0L,
                workoutPlanId = planId,
                exerciseId = exerciseId,
                positionId = positionId,
                reps = 10, sets = 4
            )
        )

        val emptyWorkoutId = workoutDao.insert(
            WorkoutEntity(0L)
        )
        val setPositionId = positionsDao.insert(
            ExercisePositionEntity(0L, workoutId = emptyWorkoutId, exerciseId = exerciseId)
        )

        val lastSet = SetEntity(
            0L,
            exerciseId = exerciseId,
            workoutId = emptyWorkoutId,
            positionId = setPositionId,
            reps = 15,
            weightInPounds = 100f,
            weightInKilograms = 50f,
            completedAt = OffsetDateTime.now().minusDays(30)
        )
        setDao.insert(lastSet)

        val workout = workoutRepository.createWorkout(
            Workout(
                id = 0L,
                addedAt = OffsetDateTime.now(),
                name = "test_workout",
                plan = null
            ),
            planId = planId
        )

        assertEquals(planId, workout.plan?.id)
        assertEquals(1, workout.entries.size)
        assertEquals(4, workout.entries.first().expectedSet?.sets)
        assertEquals(4, workout.entries.first().sets.size)
        assertEquals("Bicep Curl", workout.entries.first().exercise?.name)


        val firstSet = workout.entries.find { it.exercise?.name == "Bicep Curl" }?.sets?.first()
        // this should match the expected set / goal
        assertEquals(10, firstSet?.reps)

        // these should match the last set
        assertEquals(100f, firstSet?.weightInPounds)
        assertEquals(50f, firstSet?.weightInKilograms)
    }

    @Test
    fun createWorkoutBasedOnPlanWithGroupTest() = runTest {
        val planId = planDao.insert(
            WorkoutPlanEntity(
                0L,
                name = "test_plan"
            )
        )

        val exerciseId = exerciseDao.insert(ExerciseEntity(0L, name = "Bicep Curl"))
        val positionId = positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, exerciseId = exerciseId)
        )
        exerciseGoalDao.insert(
            ExerciseGoal(
                0L,
                workoutPlanId = planId,
                exerciseId = exerciseId,
                positionId = positionId,
                reps = 10, sets = 4
            )
        )

        val groupId = groupDao.insert(
            ExerciseGroupEntity(0L, name = "test_group")
        )
        groupDao.insertAllRefs(listOf(GroupExerciseXRef(0L, exerciseId, groupId)))
        positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, groupId = groupId, position = 2)
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

        val workout = workoutRepository.createWorkout(
            Workout(
                id = 0L,
                addedAt = OffsetDateTime.now(),
                name = "test_workout",
                plan = null
            ),
            planId = planId
        )

        assertEquals(planId, workout.plan?.id)
        assertEquals(2, workout.entries.size)
        assertEquals(4, workout.entries.first().sets.size)
        assertEquals("Bicep Curl", workout.entries.first().exercise?.name)
        assertEquals(4, workout.entries.first().expectedSet?.sets)

        val expectedSets = workout.entries.mapNotNull { it.expectedSet }
        assertEquals(
            "Had ${expectedSets.size} expected sets" +
                    " (${expectedSets.joinToString { it.exercise?.name ?: it.exerciseGroup?.name ?: "n/a" }})," +
                    " ${expectedSets.filter { it.exerciseGroup != null }.size} groups," +
                    " ${expectedSets.filter { it.exercise != null }.size} exercises.",
            1, workout.entries.filter { it.expectedSet?.exerciseGroup != null }.size)
        assertEquals("test_group", workout.entries.find { it.expectedSet?.exerciseGroup != null }?.name)
    }

}