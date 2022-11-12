package com.catscoffeeandkitchen.fitnessjournal.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen.WorkoutsScreen.resourceId
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen.WorkoutsScreen.route

sealed class FitnessJournalScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector =  Icons.Default.FitnessCenter
) {
    object Settings : FitnessJournalScreen("settings", R.string.settings, Icons.Default.Settings)
    object WorkoutsScreen : FitnessJournalScreen("workouts", R.string.workouts)
    object WorkoutPlansScreen : FitnessJournalScreen("plans", R.string.workout_plans, Icons.Default.Checklist)
    object WorkoutDetails : FitnessJournalScreen("workouts", R.string.workout_details)
    object NewWorkoutScreen : FitnessJournalScreen("workouts/new", R.string.new_workout)
    object StatsScreen : FitnessJournalScreen("stats", R.string.stats, Icons.Default.BarChart)
    object SearchExercisesScreen : FitnessJournalScreen("exercises", R.string.search_exercises)
    object WorkoutPlanEditScreen : FitnessJournalScreen("plans", R.string.workout_plan)
}