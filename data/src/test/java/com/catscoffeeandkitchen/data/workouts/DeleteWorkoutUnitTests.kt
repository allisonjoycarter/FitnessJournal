package com.catscoffeeandkitchen.data.workouts

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.catscoffeeandkitchen.data.TestDataModule
import com.catscoffeeandkitchen.data.workouts.db.*
import com.catscoffeeandkitchen.data.workouts.models.WorkoutEntity
import com.catscoffeeandkitchen.data.workouts.repository.WorkoutRepositoryImpl
import com.catscoffeeandkitchen.data.workouts.util.toWorkout
import com.catscoffeeandkitchen.domain.interfaces.WorkoutRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class DeleteWorkoutUnitTests {
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
    fun deleteWorkoutTest() = runTest {
        val insertedWorkout = workoutDao.insert(WorkoutEntity(0L))

        val workout = workoutDao.getWorkout(id = insertedWorkout)
        workoutRepository.deleteWorkout(workout.toWorkout())

        val result = workoutDao.getWorkout(insertedWorkout)
        assertEquals(null, result)
    }

}