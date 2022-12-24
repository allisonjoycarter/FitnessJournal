package com.catscoffeeandkitchen.fitnessjournal.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.catscoffeeandkitchen.fitnessjournal.R

sealed class FitnessJournalScreen(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val iconId: Int = R.drawable.fitness_center,
    val icon: ImageVector? = null,
    val testTag: String = "NavItem"
) {
    object Settings : FitnessJournalScreen(
        route = "settings",
        resourceId = R.string.settings,
        icon = Icons.Default.Settings,
        testTag = "SettingsNavItem"
    )

    object HomeScreen : FitnessJournalScreen(
        route = "home",
        resourceId = R.string.home,
        icon = Icons.Default.Home,
        testTag = "HomeNavItem"
    )

    object WorkoutsScreen : FitnessJournalScreen(
        route = "workouts",
        resourceId = R.string.workouts,
        testTag = "WorkoutsNavItem"
    )
    object WorkoutPlansScreen : FitnessJournalScreen(
        route = "plans",
        resourceId = R.string.workout_plans,
        iconId = R.drawable.checklist,
        testTag = "WorkoutPlansNavItem"
    )
    object WorkoutDetails : FitnessJournalScreen(
        route = "workouts",
        resourceId = R.string.workout_details,
        testTag = "WorkoutDetailsNavItem"
    )
    object NewWorkoutScreen : FitnessJournalScreen(
        route = "workouts/new",
        resourceId =  R.string.new_workout,
        testTag = "NewWorkoutNavItem"
    )
    object StatsScreen : FitnessJournalScreen(
        route = "stats",
        resourceId = R.string.stats,
        iconId = R.drawable.bar_chart,
        testTag = "StatsNavItem"
    )
    object SearchExercisesScreen : FitnessJournalScreen(
        route = "exercises",
        resourceId = R.string.search_exercises,
        testTag = "SearchExercisesNavItem"
    )
    object SearchExercisesMultiSelectScreen : FitnessJournalScreen(
        route = "exercises/multiselect",
        resourceId = R.string.search_exercises_multi,
        testTag = "SelectMultipleExercisesNavItem"
    )
    object WorkoutPlanEditScreen : FitnessJournalScreen(
        route = "plans",
        resourceId = R.string.workout_plan,
        testTag = "WorkoutPlanEditNavItem"
    )
    object ExerciseGroupScreen : FitnessJournalScreen(
        route = "exercises/groups",
        resourceId = R.string.exercise_groups,
        iconId = R.drawable.dataset,
        testTag = "ExerciseGroupNavItem"
    )
}