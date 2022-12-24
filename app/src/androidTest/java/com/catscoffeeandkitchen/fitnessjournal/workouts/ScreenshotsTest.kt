package com.catscoffeeandkitchen.fitnessjournal.workouts

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.catscoffeeandkitchen.data.workouts.db.ExerciseDao
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.exercise.ExerciseEntity
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.fitnessjournal.MainActivity
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.Navigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.*
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.locale.LocaleTestRule
import javax.inject.Inject

@HiltAndroidTest
@RunWith(JUnit4::class)
class ScreenshotsTest {

    @ClassRule
    val localeTestRule: LocaleTestRule = LocaleTestRule()

    @get:Rule(order = 1)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: FitnessJournalDb

    @Inject
    lateinit var exerciseDao: ExerciseDao

    @Before
    fun setup() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        CleanStatusBar.enableWithDefaults()
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

    @After
    fun teardown() {
        CleanStatusBar.disable()
    }

    @Test
    fun testWithScreenshots() {
        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent).assertExists()
        Screengrab.screenshot("home_screen")

        composeTestRule.onNodeWithTag(FitnessJournalScreen.WorkoutsScreen.testTag)
            .performClick()

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

        // scroll down
        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent)
            .performScrollToIndex(2)

        composeTestRule.onNodeWithTag(TestTags.AddExerciseButton)
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

        composeTestRule.onAllNodesWithTag(TestTags.EditableSet).assertCountEquals(2)

        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent).performScrollToKey(2)
        composeTestRule.onAllNodesWithTag(TestTags.AddSetButton)
            .onLast()
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent).performTouchInput { swipeUp() }
        composeTestRule
            .onAllNodesWithTag(TestTags.EditableExercise).onLast()
            .onChildren()
            .filter(hasTestTag(TestTags.EditableSet))
            .assertCountEquals(2)

        // check first exercise complete
        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent).performScrollToKey(1)
        for (i in 0 until 2) {
            composeTestRule.onAllNodesWithTag(TestTags.CompleteSetCheckbox)[i].performClick()
        }

        Screengrab.screenshot("workout_in_progress")

        composeTestRule.onAllNodesWithTag(TestTags.ReadOnlyExercise).assertCountEquals(1)
        composeTestRule.onAllNodesWithTag(TestTags.EditableExercise).assertCountEquals(1)

        // add another exercise
        composeTestRule.onNodeWithTag(TestTags.AddExerciseButton)
            .performClick()

        composeTestRule.onNodeWithTag(TestTags.ScrollableComponent).performScrollToIndex(3)
        composeTestRule.onAllNodesWithTag(TestTags.ExerciseSearchResult)[2]
            .performClick()

        composeTestRule.onAllNodesWithTag(TestTags.ReadOnlyExercise).assertCountEquals(1)
        composeTestRule.onAllNodesWithTag(TestTags.EditableExercise).assertCountEquals(2)

        composeTestRule.onAllNodesWithTag(TestTags.CompleteSetCheckbox).assertCountEquals(3)

    }
}