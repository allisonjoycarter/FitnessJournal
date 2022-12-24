@file:OptIn(ExperimentalMaterial3Api::class)

package com.catscoffeeandkitchen.fitnessjournal.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.catscoffeeandkitchen.domain.usecases.data.BackupDataUseCase
import com.catscoffeeandkitchen.domain.usecases.data.RestoreDataUseCase
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.home.HomeScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.settings.SettingsScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.SelectPlanScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.WorkoutDetailsScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.exercisegroups.ExerciseGroupScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.list.WorkoutsScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.WorkoutPlanEditScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.list.WorkoutPlansScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises.SearchExercisesMultiSelectScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises.SearchExercisesScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats.StatsScreen
import com.github.mikephil.charting.animation.Easing
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    val navController = rememberAnimatedNavController()

    AnimatedNavHost(
        navController,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        startDestination = FitnessJournalScreen.HomeScreen.route,
        enterTransition = { fadeIn(initialAlpha = .3f, animationSpec = tween(easing = FastOutSlowInEasing)) },
        exitTransition = { fadeOut(animationSpec = tween(easing = FastOutSlowInEasing)) },
    ) {
        composable(
            FitnessJournalScreen.HomeScreen.route,
            deepLinks = listOf(navDeepLink { uriPattern = "liftinglog://app/${FitnessJournalScreen.HomeScreen.route}" }),
        ) {
            Scaffold(
                topBar = { FitnessJournalTopAppBar {
                    navController.navigate(FitnessJournalScreen.Settings.route)
                }},
                bottomBar = {
                    FitnessJournalBottomNavigationBar(navController = navController)
                },
            )  { padding ->
                HomeScreen(
                    navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            FitnessJournalScreen.WorkoutsScreen.route,
            deepLinks = listOf(navDeepLink { uriPattern = "liftinglog://app/${FitnessJournalScreen.WorkoutsScreen.route}" }),
            ) {
            Scaffold(
                topBar = { FitnessJournalTopAppBar {
                    navController.navigate(FitnessJournalScreen.Settings.route)
                }},
                bottomBar = {
                    FitnessJournalBottomNavigationBar(navController = navController)
                },
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(
                                FitnessJournalScreen.NewWorkoutScreen.route
                            )
                        },
                        modifier = Modifier.testTag(TestTags.FAB)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "create workout")
                    }
                }
            )  { padding ->
                WorkoutsScreen(
                    navController,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            FitnessJournalScreen.WorkoutPlansScreen.route,
        ) {
            Scaffold(
                topBar = { FitnessJournalTopAppBar {
                    navController.navigate(FitnessJournalScreen.Settings.route)
                }},
                bottomBar = {
                    FitnessJournalBottomNavigationBar(navController = navController)
                },
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(
                                "${FitnessJournalScreen.WorkoutPlanEditScreen.route}/0"
                            )
                        },
                        modifier = Modifier.testTag(TestTags.FAB)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "create plan")
                    }
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
            deepLinks = listOf(navDeepLink {
                uriPattern = "liftinglog://app/${FitnessJournalScreen.WorkoutDetails.route}/{workoutId}"
            }),
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
                topBar = { FitnessJournalTopAppBar {
                    navController.navigate(FitnessJournalScreen.Settings.route)
                }},
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
            "${FitnessJournalScreen.SearchExercisesScreen.route}?" +
                    "category={category}&muscle={muscle}",
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("muscle") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            )
        ) { entry ->
            val muscle = entry.arguments?.getString("muscle")
            val category = entry.arguments?.getString("category")

            Scaffold() { padding ->
                SearchExercisesScreen(
                    navController,
                    muscle = muscle,
                    category = category,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            "${FitnessJournalScreen.SearchExercisesMultiSelectScreen.route}?" +
                    "category={category}&muscle={muscle}&selectedExercises={selectedExercises}",
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("muscle") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("selectedExercises") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val muscle = entry.arguments?.getString("muscle")
            val category = entry.arguments?.getString("category")

            SearchExercisesMultiSelectScreen(
                navController,
                muscle = muscle,
                category = category,
            )
        }

        composable(
            "${FitnessJournalScreen.ExerciseGroupScreen.route}?selectable={selectable}",
            arguments = listOf(
                navArgument("selectable") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val selectable = backStackEntry.arguments?.getBoolean("selectable")

            Scaffold(
                floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(
                                FitnessJournalScreen.SearchExercisesMultiSelectScreen.route
                            )
                        },
                        modifier = Modifier.testTag(TestTags.FAB)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "create group")
                    }
                }
            ) { padding ->
                ExerciseGroupScreen(
                    navController,
                    selectable = selectable ?: false,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(
            FitnessJournalScreen.Settings.route
        )  {
            Scaffold() { padding ->
                SettingsScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessJournalTopAppBar(
    navigateToSettings: () -> Unit,
) {
    TopAppBar(
        title = {},
        actions = {
            IconButton(
                onClick = { navigateToSettings() },
                modifier = Modifier.testTag(FitnessJournalScreen.Settings.testTag)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "go to settings"
                )
            }
        }
    )
}

@Composable
fun FitnessJournalBottomNavigationBar(
    navController: NavController
) {
    NavigationBar(
        Modifier.testTag(TestTags.BottomNavigationBar)
    ) {
        NavigationBarItem(
            selected = navController.currentDestination?.route?.contains("home") == true,
            onClick = { navController.navigate(FitnessJournalScreen.HomeScreen.route) },
            icon = { BottomBarIcon(screen = FitnessJournalScreen.HomeScreen) },
            label = { Text("Home") },
            modifier = Modifier.testTag(FitnessJournalScreen.HomeScreen.testTag)
        )

        NavigationBarItem(
            selected = navController.currentDestination?.route?.contains("workout") == true,
            onClick = { navController.navigate(FitnessJournalScreen.WorkoutsScreen.route) },
            icon = { BottomBarIcon(screen = FitnessJournalScreen.WorkoutsScreen) },
            label = { Text("Workouts") },
            modifier = Modifier.testTag(FitnessJournalScreen.WorkoutsScreen.testTag)
        )

        NavigationBarItem(
            selected = navController.currentDestination?.route?.contains("plans") == true,
            onClick = { navController.navigate(FitnessJournalScreen.WorkoutPlansScreen.route) },
            icon = { BottomBarIcon(screen = FitnessJournalScreen.WorkoutPlansScreen) },
            label = { Text("Plans") },
            modifier = Modifier.testTag(FitnessJournalScreen.WorkoutPlansScreen.testTag)
        )

        NavigationBarItem(
            selected = navController.currentDestination?.route?.contains("stats") == true,
            onClick = { navController.navigate(FitnessJournalScreen.StatsScreen.route) },
            icon = { BottomBarIcon(screen = FitnessJournalScreen.StatsScreen) },
            label = { Text("Stats") },
            modifier = Modifier.testTag(FitnessJournalScreen.StatsScreen.testTag)
        )
    }
}

@Composable
fun BottomBarIcon(screen: FitnessJournalScreen) {
    if (screen.icon != null) {
        Icon(screen.icon, stringResource(id = screen.resourceId))
    } else {
        Icon(painterResource(id = screen.iconId), stringResource(id = screen.resourceId))
    }
}
