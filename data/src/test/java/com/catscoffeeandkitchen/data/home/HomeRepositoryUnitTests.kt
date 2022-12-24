package com.catscoffeeandkitchen.data.home

import androidx.compose.ui.geometry.Offset
import androidx.test.platform.app.InstrumentationRegistry
import com.catscoffeeandkitchen.data.TestDataModule
import com.catscoffeeandkitchen.data.workouts.db.*
import com.catscoffeeandkitchen.data.workouts.models.*
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExercisePositionEntity
import com.catscoffeeandkitchen.data.workouts.repository.HomeRepositoryImpl
import com.catscoffeeandkitchen.data.workouts.repository.WorkoutRepositoryImpl
import com.catscoffeeandkitchen.domain.interfaces.HomeRepository
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Workout
import io.mockk.every
import io.mockk.mockkStatic
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeRepositoryUnitTests {
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
    private lateinit var homeRepository: HomeRepository

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

        homeRepository = HomeRepositoryImpl(db)

        Dispatchers.setMain(testDispatcher)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        Dispatchers.resetMain()
        db.close()
    }

    @Test
    fun getNullNextWorkoutPlanTest() = runTest {
        val plan = homeRepository.getNextWorkoutPlan()
        assertNull(plan)

        planDao.insert(WorkoutPlanEntity(
            0L,
            addedAt = OffsetDateTime.now(),
            name = "test_plan",
            note = null,
            daysOfWeek = listOf(DayOfWeek.SUNDAY.name)
        ))

        // ensure the plan will not be on today
        mockkStatic(OffsetDateTime::class)
        val date = OffsetDateTime.of(
            2022,
            10,
            10,
            10,
            10,
            10,
            10,
            ZoneOffset.UTC
        )
        every { OffsetDateTime.now() } returns date.with(DayOfWeek.MONDAY)

        val nextPlan = homeRepository.getNextWorkoutPlan()
        assertNull(nextPlan)
    }

    @Test
    fun getNextWorkoutPlanText() = runTest(testDispatcher) {
        val planId = planDao.insert(WorkoutPlanEntity(
            0L,
            addedAt = OffsetDateTime.now(),
            name = "test_plan",
            note = null,
            daysOfWeek = listOf(DayOfWeek.SUNDAY.name)
        ))

        val exerciseId = exerciseDao.insert(
            ExerciseEntity(0L, name = "Bicep Curl")
        )

        val groupId = groupDao.insert(
            ExerciseGroupEntity(0L, name = "test_group")
        )
        groupDao.insertAllRefs(listOf(GroupExerciseXRef(0L, exerciseId, groupId)))
        val positionId = positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, groupId = groupId, position = 1)
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

        val position2Id = positionsDao.insert(
            ExercisePositionEntity(
                0L, planId = planId, groupId = groupId, exerciseId = exerciseId, position = 2)
        )
        exerciseGoalDao.insert(
            ExerciseGoal(
                0L,
                workoutPlanId = planId,
                exerciseGroupId = groupId,
                positionId = position2Id,
                reps = 10, sets = 4
            )
        )
        positionsDao.insert(
            ExercisePositionEntity(
                0L, planId = planId, position = 3)
        )

        positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, exerciseId = exerciseId, position = 4)
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

        positionsDao.insert(
            ExercisePositionEntity(0L, planId = planId, position = 5)
        )
        exerciseGoalDao.insert(
            ExerciseGoal(
                0L,
                workoutPlanId = planId,
                positionId = positionId,
                reps = 10, sets = 4
            )
        )

        // ensure the plan is today
        mockkStatic(OffsetDateTime::class)
        val date = OffsetDateTime.of(
            2022,
            10,
            10,
            10,
            10,
            10,
            10,
            ZoneOffset.UTC
        )
        every { OffsetDateTime.now() } returns date.with(DayOfWeek.SUNDAY)

        val nextPlan = homeRepository.getNextWorkoutPlan()
        assertEquals(planId, nextPlan?.id)
        assertEquals(3, nextPlan?.entries?.size)
    }

    @Test
    fun getWeekStatsTest() = runTest {
        val emptyStats = homeRepository.getWorkoutWeekStats(1)
        assert(emptyStats.dates.isEmpty())

        workoutDao.insert(
            WorkoutEntity(
                0L,
                addedAt = OffsetDateTime.now(),
                name = "test_workout",
                completedAt = null
            )
        )
        val notCompletedStats = homeRepository.getWorkoutWeekStats(1)
        assert(notCompletedStats.dates.isEmpty())

        val completedTime = OffsetDateTime.now(ZoneId.systemDefault())
            .with(DayOfWeek.SUNDAY)
            .withHour(7)
            .withNano(0)
            .withSecond(0)

        workoutDao.insert(
            WorkoutEntity(
                0L,
                addedAt = completedTime.minusHours(1),
                name = "test_workout",
                completedAt = completedTime
            )
        )

        workoutDao.insert(
            WorkoutEntity(
                0L,
                addedAt = completedTime.minusHours(1),
                name = "test_workout",
                completedAt = null
            )
        )

        workoutDao.insert(
            WorkoutEntity(
                0L,
                addedAt = completedTime.minusHours(1),
                name = "test_workout",
                completedAt = completedTime
            )
        )

        val weekStats = homeRepository.getWorkoutWeekStats(1)
        val utcTime = completedTime.withOffsetSameInstant(ZoneOffset.UTC)

        assertEquals(listOf(utcTime, utcTime), weekStats.dates)
        assertEquals(2.0, weekStats.averageWorkoutsPerWeek)
        assertEquals(listOf(DayOfWeek.SUNDAY), weekStats.mostCommonDays)
        assertEquals(listOf(completedTime.hour), weekStats.mostCommonTimes)
    }

    @Test
    fun getNullImprovedExerciseTest() = runTest(testDispatcher) {
        val nullExercise = homeRepository.getMostImprovedExercise(2)
        assertNull(nullExercise)

        val workoutId = workoutDao.insert(
            WorkoutEntity(
                0L,
                addedAt = OffsetDateTime.now().minusDays(5),
                name = "test_workout",
            ))

        val bicepCurlId = exerciseDao.insert(ExerciseEntity(
            0L,
            name = "Bicep Curl"
        ))

        val bicepCurlPosition = positionsDao.insert(
            ExercisePositionEntity(
                0L,
                workoutId = workoutId,
                exerciseId = bicepCurlId,
            )
        )

        val firstDate = OffsetDateTime.now().minusDays(5)
        setDao.insertAll(listOf(
            SetEntity(
                0L,
                exerciseId = bicepCurlId,
                workoutId = workoutId,
                positionId = bicepCurlPosition,
                reps = 10,
                weightInPounds = 100f,
                completedAt = firstDate
            ),
        ))

        val noWorstSetExercise = homeRepository.getMostImprovedExercise(2)
        assertNull(noWorstSetExercise)
    }

    @Test
    fun getImprovedExerciseTest() = runTest(testDispatcher) {
        val workoutId = workoutDao.insert(
            WorkoutEntity(
            0L,
            addedAt = OffsetDateTime.now().minusDays(5),
            name = "test_workout",
        ))

        val secondWorkoutId = workoutDao.insert(
            WorkoutEntity(
            0L,
            addedAt = OffsetDateTime.now(),
            name = "test_workout2",
        ))

        val bicepCurlId = exerciseDao.insert(ExerciseEntity(
            0L,
            name = "Bicep Curl"
        ))
        val benchPressId = exerciseDao.insert(ExerciseEntity(
            0L,
            name = "Bench Press"
        ))

        val bicepCurlPosition = positionsDao.insert(
            ExercisePositionEntity(
                0L,
                workoutId = workoutId,
                exerciseId = bicepCurlId,
            )
        )

        val benchPressPosition = positionsDao.insert(
            ExercisePositionEntity(
                0L,
                workoutId = workoutId,
                exerciseId = benchPressId,
                position = 2
            )
        )

        val bicepCurlPosition2 = positionsDao.insert(
            ExercisePositionEntity(
                0L,
                workoutId = secondWorkoutId,
                exerciseId = bicepCurlPosition,
            )
        )

        val benchPressPosition2 = positionsDao.insert(
            ExercisePositionEntity(
                0L,
                workoutId = secondWorkoutId,
                exerciseId = benchPressId,
                position = 2
            )
        )

        val firstDate = OffsetDateTime.now().minusDays(5)
        val secondDate = OffsetDateTime.now()

        setDao.insertAll(listOf(
            SetEntity(
                0L,
                exerciseId = bicepCurlId,
                workoutId = workoutId,
                positionId = bicepCurlPosition,
                reps = 10,
                weightInPounds = 100f,
                completedAt = firstDate
            ),
            SetEntity(
                0L,
                exerciseId = bicepCurlId,
                workoutId = workoutId,
                positionId = bicepCurlPosition,
                reps = 10,
                weightInPounds = 100f,
                setNumber = 2,
                completedAt = firstDate
            ),
            SetEntity(
                0L,
                exerciseId = bicepCurlId,
                workoutId = workoutId,
                positionId = bicepCurlPosition,
                reps = 10,
                weightInPounds = 100f,
                setNumber = 3,
                completedAt = firstDate
            ),
            SetEntity(
                0L,
                exerciseId = benchPressId,
                workoutId = workoutId,
                positionId = benchPressPosition,
                reps = 10,
                weightInPounds = 100f,
                completedAt = firstDate
            ),
            SetEntity(
                0L,
                exerciseId = benchPressId,
                workoutId = workoutId,
                positionId = benchPressPosition,
                reps = 10,
                weightInPounds = 100f,
                setNumber = 2,
                completedAt = firstDate
            ),
            SetEntity(
                0L,
                exerciseId = benchPressId,
                workoutId = workoutId,
                positionId = benchPressPosition,
                reps = 10,
                weightInPounds = 100f,
                setNumber = 3,
                completedAt = firstDate
            ),
            SetEntity(
                0L,
                exerciseId = bicepCurlId,
                workoutId = secondWorkoutId,
                positionId = bicepCurlPosition2,
                reps = 10,
                weightInPounds = 105f,
                setNumber = 2,
                completedAt = secondDate
            ),
            SetEntity(
                0L,
                exerciseId = benchPressId,
                workoutId = secondWorkoutId,
                positionId = benchPressPosition2,
                reps = 10,
                weightInPounds = 120f,
                setNumber = 2,
                completedAt = secondDate
            ),
            SetEntity(
                0L,
                exerciseId = benchPressId,
                workoutId = secondWorkoutId,
                positionId = benchPressPosition2,
                reps = 10,
                weightInPounds = 120f,
                setNumber = 3,
                completedAt = secondDate
            )
        ))

        val progressStats = homeRepository.getMostImprovedExercise(3)

        assertEquals("Bench Press", progressStats?.exercise?.name)
        assertEquals(3 * 7L, progressStats?.amountOfTime?.toDays())

        val expectedStarting1RM = 100f / (1.0278 - 0.0278 * 10)
        val expectedEnding1RM = 120f / (1.0278 - 0.0278 * 10)
        assertEquals(expectedStarting1RM.roundToInt(), progressStats?.starting1RM?.roundToInt())
        assertEquals(expectedEnding1RM.roundToInt(), progressStats?.ending1RM?.roundToInt())
    }

    @Test
    fun getEmptyLastExercisesTest() = runTest {
        val empty = homeRepository.getLastExercisesCompleted()
        assertEquals(0, empty.size)
    }

    @Test
    fun getLastExercisesTest() = runTest {
        val workoutId = workoutDao.insert(
            WorkoutEntity(
                0L,
                addedAt = OffsetDateTime.now(),
                name = "test_workout",
                completedAt = OffsetDateTime.now()
            ))

        val bicepCurlId = exerciseDao.insert(ExerciseEntity(
            0L,
            name = "Bicep Curl"
        ))

        val bicepCurlPosition = positionsDao.insert(
            ExercisePositionEntity(
                0L,
                workoutId = workoutId,
                exerciseId = bicepCurlId,
            )
        )

        val groupId = groupDao.insert(
            ExerciseGroupEntity(0L, name = "test_group")
        )
        groupDao.insertAllRefs(listOf(GroupExerciseXRef(0L, bicepCurlId, groupId)))
        val positionId = positionsDao.insert(
            ExercisePositionEntity(0L, workoutId = workoutId, groupId = groupId, position = 1)
        )

        val planId = planDao.insert(
            WorkoutPlanEntity(0L)
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

        val firstDate = OffsetDateTime.now().minusDays(5)
        setDao.insertAll(listOf(
            SetEntity(
                0L,
                exerciseId = bicepCurlId,
                workoutId = workoutId,
                positionId = bicepCurlPosition,
                reps = 10,
                weightInPounds = 100f,
                completedAt = firstDate
            ),
            SetEntity(
                0L,
                exerciseId = bicepCurlId,
                workoutId = workoutId,
                positionId = bicepCurlPosition,
                reps = 10,
                weightInPounds = 100f,
                setNumber = 2,
                completedAt = firstDate
            ),
        ))

        val lastExercises = homeRepository.getLastExercisesCompleted()
        assertEquals(2, lastExercises.size)
        assertEquals(2, lastExercises.first { it.exercise != null }.sets.size)
        assertEquals("Bicep Curl", lastExercises.first { it.exercise != null }.exercise?.name)
    }
}