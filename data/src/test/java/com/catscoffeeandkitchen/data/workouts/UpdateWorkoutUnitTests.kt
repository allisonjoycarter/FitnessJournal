package com.catscoffeeandkitchen.data.workouts

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.catscoffeeandkitchen.data.TestDataModule
import com.catscoffeeandkitchen.data.workouts.db.*
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity
import com.catscoffeeandkitchen.data.workouts.repository.WorkoutRepositoryImpl
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import com.catscoffeeandkitchen.domain.models.Workout
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class UpdateWorkoutUnitTests {
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
        db.close()
    }

    @Test
    fun updateWorkoutTest() = runTest {
        val insertedWorkout = workoutDao.insert(WorkoutEntity(0L))

        val completedAt = OffsetDateTime.of(2022, 1, 1, 0,0,0,0, ZoneOffset.of("Z"))
        val updatedWorkout = workoutRepository.updateWorkout(Workout(
            id = insertedWorkout,
            name = "updated name",
            note = "updated note",
            completedAt = completedAt
        ))

        assertEquals("updated name", updatedWorkout.name)
        assertEquals("updated note", updatedWorkout.note)
        assertEquals(completedAt, updatedWorkout.completedAt)

        val databaseWorkout = workoutDao.getWorkout(insertedWorkout)
        assertEquals("updated name", databaseWorkout.name)
        assertEquals("updated note", databaseWorkout.note)
        assertEquals(completedAt, databaseWorkout.completedAt)
    }

}