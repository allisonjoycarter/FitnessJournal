package com.catscoffeeandkitchen.fitnessjournal.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.SelectPlanScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.WorkoutDetailsScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.list.WorkoutsScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.WorkoutPlanEditScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.list.WorkoutPlansScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises.SearchExercisesScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats.StatsScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController,
        startDestination = FitnessJournalScreen.WorkoutPlansScreen.route,
    ) {
        composable(
            FitnessJournalScreen.WorkoutsScreen.route
        ) {
            Scaffold(
                bottomBar = {
                    FitnessJournalBottomNavigationBar(navController = navController)
                },
            )  { padding ->
                WorkoutsScreen(navController, modifier = Modifier
                    .padding(padding)
                )
            }
        }

        composable(
            FitnessJournalScreen.WorkoutPlansScreen.route
        ) {
            Scaffold(
                bottomBar = {
                    FitnessJournalBottomNavigationBar(navController = navController)
                }
            )  { padding ->
                WorkoutPlansScreen(navController, modifier = Modifier
                    .padding(padding)
                )
            }
        }

        composable(
            FitnessJournalScreen.NewWorkoutScreen.route,
        ) {
            Scaffold { padding ->
                SelectPlanScreen(
                    navController = navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            "${FitnessJournalScreen.WorkoutDetails.route}/{workoutId}?plan={plan}",
            arguments = listOf(
                navArgument("workoutId") { type = NavType.LongType },
                navArgument("plan") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            )
        ) {
            Scaffold { padding ->
                WorkoutDetailsScreen(
                    navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            "${FitnessJournalScreen.WorkoutPlanEditScreen.route}/{workoutId}",
            arguments = listOf(
                navArgument("workoutId") { type = NavType.LongType },
            )
        ) {
            Scaffold() { padding ->
                WorkoutPlanEditScreen(
                    navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            FitnessJournalScreen.StatsScreen.route
        ) {
            Scaffold(
                bottomBar = {
                    FitnessJournalBottomNavigationBar(navController = navController)
                }
            )  { padding ->
                StatsScreen(modifier = Modifier
                    .padding(padding)
                )
            }
        }

        composable(
            FitnessJournalScreen.SearchExercisesScreen.route
        ) {
            Scaffold() { padding ->
                SearchExercisesScreen(
                    navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun FitnessJournalBottomNavigationBar(
    navController: NavController
) {
    NavigationBar() {
        NavigationBarItem(
            selected = navController.currentDestination?.route?.contains("plans") == true,
            onClick = { navController.navigate(FitnessJournalScreen.WorkoutPlansScreen.route) },
            icon = { Icon(FitnessJournalScreen.WorkoutPlansScreen.icon, "plans") },
            label = { Text("Plans") },
        )

        NavigationBarItem(
            selected = navController.currentDestination?.route?.contains("workout") == true,
            onClick = { navController.navigate(FitnessJournalScreen.WorkoutsScreen.route) },
            icon = { Icon(FitnessJournalScreen.WorkoutsScreen.icon, "workouts") },
            label = { Text("Workouts") }
        )

        NavigationBarItem(
            selected = navController.currentDestination?.route?.contains("stats") == true,
            onClick = { navController.navigate(FitnessJournalScreen.StatsScreen.route) },
            icon = { Icon(FitnessJournalScreen.StatsScreen.icon, "stats") },
            label = { Text("Stats") }
        )
    }
}
