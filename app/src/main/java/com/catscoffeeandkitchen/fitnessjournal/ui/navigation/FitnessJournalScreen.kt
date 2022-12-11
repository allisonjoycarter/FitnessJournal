package com.catscoffeeandkitchen.fitnessjournal.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.catscoffeeandkitchen.fitnessjournal.R

sealed class FitnessJournalScreen(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val iconId: Int = R.drawable.fitness_center,
    val icon: ImageVector? = null
) {
    object Settings : FitnessJournalScreen("settings", R.string.settings, icon = Icons.Default.Settings)
    object WorkoutsScreen : FitnessJournalScreen("workouts", R.string.workouts)
    object WorkoutPlansScreen : FitnessJournalScreen("plans", R.string.workout_plans, R.drawable.checklist)
    object WorkoutDetails : FitnessJournalScreen("workouts", R.string.workout_details)
    object NewWorkoutScreen : FitnessJournalScreen("workouts/new", R.string.new_workout)
    object StatsScreen : FitnessJournalScreen("stats", R.string.stats, R.drawable.bar_chart)
    object SearchExercisesScreen : FitnessJournalScreen("exercises", R.string.search_exercises)
    object SearchExercisesMultiSelectScreen : FitnessJournalScreen("exercises/multiselect", R.string.search_exercises_multi)
    object WorkoutPlanEditScreen : FitnessJournalScreen("plans", R.string.workout_plan)
    object ExerciseGroupScreen : FitnessJournalScreen("exercises/groups", R.string.exercise_groups, R.drawable.dataset)
}