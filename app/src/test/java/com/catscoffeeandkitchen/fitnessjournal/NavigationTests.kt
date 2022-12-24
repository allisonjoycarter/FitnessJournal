package com.catscoffeeandkitchen.fitnessjournal

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.Navigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(
    application = HiltTestApplication::class,
    instrumentedPackages = [
        // required to access final members on androidx.loader.content.ModernAsyncTask
        "androidx.loader.content"
    ]
)
@RunWith(RobolectricTestRunner::class)
class NavigationTests {

    @get:Rule(order = 1)
    var hiltTestRule = HiltAndroidRule(this)

    @get:Rule(order = 2)
    var composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltTestRule.inject()
        composeTestRule.setContent {
            Navigation()
        }
    }


    @Test
    fun navigateToHome() {
        composeTestRule.onNodeWithTag(FitnessJournalScreen.HomeScreen.testTag)
            .performClick()

        composeTestRule
            .onNodeWithTag(TestTags.BottomNavigationBar)
            .onChild()
            .onChildAt(0)
            .assertIsSelected()
    }

    @Test
    fun navigateToPlans() {
        composeTestRule.onNodeWithTag(FitnessJournalScreen.WorkoutPlansScreen.testTag)
            .performClick()

        composeTestRule
            .onNodeWithTag(TestTags.BottomNavigationBar)
            .onChild()
            .onChildAt(2)
            .assertIsSelected()
    }

    @Test
    fun navigateToStats() {
        composeTestRule.onNodeWithTag(FitnessJournalScreen.StatsScreen.testTag)
            .performClick()

        composeTestRule
            .onNodeWithTag(TestTags.BottomNavigationBar)
            .onChild()
            .onChildAt(3)
            .assertIsSelected()
    }

    @Test
    fun navigateToSettings() {
        composeTestRule.onNodeWithTag(FitnessJournalScreen.Settings.testTag)
            .performClick()

        composeTestRule
            .onNodeWithText("Settings")
            .assertIsDisplayed()
    }

}