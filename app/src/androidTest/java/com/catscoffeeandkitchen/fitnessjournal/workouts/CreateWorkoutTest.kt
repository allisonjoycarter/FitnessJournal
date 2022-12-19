package com.catscoffeeandkitchen.fitnessjournal.workouts

import android.os.Environment
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.InstrumentationRegistry.getInstrumentation
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.catscoffeeandkitchen.data.workouts.db.ExerciseDao
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.fitnessjournal.MainActivity
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.Navigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltAndroidTest
class CreateWorkoutTest {

    @get:Rule(order = 1)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var idlingResource: IdlingResource

    @Inject
    lateinit var database: FitnessJournalDb

    @Inject
    lateinit var exerciseDao: ExerciseDao

    @get:Rule(order = 3)
    val watcher = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description?) {
            // Save to external storage (usually /sdcard/screenshots)
            val path = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).absolutePath + "/screenshots/" +
                    InstrumentationRegistry.getInstrumentation().targetContext.packageName
            )
            if (!path.exists()) {
                path.mkdirs()
            }

            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val filename = description?.className + "-" + description?.methodName + ".png"
            device.takeScreenshot(File(path, filename))
        }
    }

    @Before
    fun setup() {
        hiltTestRule.inject()

        runBlocking {
            exerciseDao.insert(
                ExerciseEntity(
                    eId = 0L,
                    name = "Bench Press",
                    musclesWorked = listOf("Pecs", "Triceps"),
                    userCreated = false,
                    category = "Chest",
                    thumbnailUrl = null,
                    equipmentType = ExerciseEquipmentType.Barbell
                )
            )

            exerciseDao.insert(
                ExerciseEntity(
                    eId = 0L,
                    name = "Shoulder Press",
                    musclesWorked = listOf("Shoulders", "Delts"),
                    userCreated = false,
                    category = "Shoulders",
                    thumbnailUrl = null,
                    equipmentType = ExerciseEquipmentType.Dumbbell
                )
            )

            exerciseDao.insert(
                ExerciseEntity(
                    eId = 0L,
                    name = "Seated Cable Row",
                    musclesWorked = listOf("Lats"),
                    userCreated = false,
                    category = "Back",
                    thumbnailUrl = null,
                    equipmentType = ExerciseEquipmentType.Cable
                )
            )

            exerciseDao.insert(
                ExerciseEntity(
                    eId = 0L,
                    name = "Chest Press",
                    musclesWorked = listOf("Pecs"),
                    userCreated = false,
                    category = "Chest",
                    thumbnailUrl = null,
                    equipmentType = ExerciseEquipmentType.Machine
                )
            )
        }

        composeTestRule.setContent {
            Navigation()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createNewWorkout() {
        composeTestRule.onNodeWithTag(TestTags.FAB)
            .performClick()

        composeTestRule
            .onNodeWithText("New Workout")
            .assertIsDisplayed()
    }

    @Test
    fun addExerciseToWorkout() {
        composeTestRule.onNodeWithTag(TestTags.FAB)
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.AddExerciseButton)
            .performClick()


        composeTestRule.onAllNodesWithTag(TestTags.ExerciseSearchResult)
            .onFirst()
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.EditableExercise)
            .assertIsDisplayed()
    }



}