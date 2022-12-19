package com.catscoffeeandkitchen.fitnessjournal

import androidx.activity.viewModels
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalBottomNavigationBar
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.Navigation
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
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
    fun navigateToPlans() {
        composeTestRule.onNodeWithTag(FitnessJournalScreen.WorkoutPlansScreen.testTag)
            .performClick()

        composeTestRule
            .onNodeWithTag(TestTags.BottomNavigationBar)
            .onChild()
            .onChildAt(1)
            .assertIsSelected()
    }

    @Test
    fun navigateToGroups() {
        composeTestRule.onNodeWithTag(FitnessJournalScreen.ExerciseGroupScreen.testTag)
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
            .onNodeWithText(composeTestRule.activity.baseContext.getString(FitnessJournalScreen.Settings.resourceId))
            .assertIsDisplayed()
    }

}