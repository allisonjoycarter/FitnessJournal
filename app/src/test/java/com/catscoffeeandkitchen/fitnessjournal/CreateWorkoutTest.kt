package com.catscoffeeandkitchen.fitnessjournal

import android.os.Environment
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.catscoffeeandkitchen.data.workouts.db.ExerciseDao
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.Navigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, instrumentedPackages = ["androidx.loader.content"])
class CreateWorkoutTest {

    @get:Rule(order = 1)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeTestRule = createComposeRule()

    @Inject
    lateinit var database: FitnessJournalDb

    @Inject
    lateinit var exerciseDao: ExerciseDao

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


    @Test
    fun performWorkout() {
        composeTestRule.onNodeWithTag(TestTags.FAB)
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.AddExerciseButton)
            .performClick()

        // add first exercise
        composeTestRule.onAllNodesWithTag(TestTags.ExerciseSearchResult)
            .onFirst()
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.EditableExercise)
            .assertIsDisplayed()

        // add second exercise
        composeTestRule.onNodeWithTag(TestTags.AddExerciseButton)
            .performClick()

        composeTestRule.onAllNodesWithTag(TestTags.ExerciseSearchResult)[1]
            .performClick()

        composeTestRule.onAllNodesWithTag(TestTags.EditableExercise)
            .assertCountEquals(2)

        // add 2 sets to each exercise
        composeTestRule.onAllNodesWithTag(TestTags.AddSetButton)
            .onFirst()
            .performClick()
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent).performScrollToKey(2)
        composeTestRule.onAllNodesWithTag(TestTags.AddSetButton)
            .onLast()
            .performClick()
            .performClick()

        composeTestRule.onAllNodesWithTag(TestTags.CompleteSetCheckbox).assertCountEquals(6)

        // check first exercise complete
        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent).performScrollToIndex(0)
        for (i in 0 until 3) {
            composeTestRule.onAllNodesWithTag(TestTags.CompleteSetCheckbox)[i].performClick()
        }

        composeTestRule.onAllNodesWithTag(TestTags.ReadOnlyExercise).assertCountEquals(1)
        composeTestRule.onAllNodesWithTag(TestTags.EditableExercise).assertCountEquals(1)

        // add another exercise
        composeTestRule.onNodeWithTag(TestTags.AddExerciseButton)
            .performClick()

        composeTestRule.onAllNodesWithTag(TestTags.ExerciseSearchResult)[2]
            .performClick()

        composeTestRule.onAllNodesWithTag(TestTags.ReadOnlyExercise).assertCountEquals(1)
        composeTestRule.onAllNodesWithTag(TestTags.EditableExercise).assertCountEquals(2)

        composeTestRule.onAllNodesWithTag(TestTags.CompleteSetCheckbox).assertCountEquals(4)

    }
}