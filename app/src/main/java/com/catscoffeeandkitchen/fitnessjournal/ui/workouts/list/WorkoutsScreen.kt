package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutsViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
//    val onStartOrResume by rememberUpdatedState(viewModel::getWorkouts)
//
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
//                onStartOrResume()
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }

//    Timber.d(viewModel.workouts.value.toString())
//        when (val workouts = viewModel.workouts.value) {
//            is DataState.Loading -> {
//                Column(
//                    modifier = modifier
//                        .fillMaxSize()
//                        .background(MaterialTheme.colorScheme.background),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Box(modifier = Modifier.fillMaxSize(.3f)) {
//                        CircularProgressIndicator()
//                    }
//                }
//            }
//            is DataState.Success -> {
//                if (workouts.data.isNotEmpty()) {
            val workouts = viewModel.pagedWorkout.collectAsLazyPagingItems()
                    var workoutToDelete by remember { mutableStateOf(null as Workout?) }

                    if (workoutToDelete != null) {
                        AlertDialog(
                            onDismissRequest = { workoutToDelete = null },
                            confirmButton = {
                                TextButton(onClick = {
                                    workoutToDelete?.let { viewModel.deleteWorkout(it) }
                                    workoutToDelete = null
                                }) {
                                    Text("Remove")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { workoutToDelete = null }) {
                                    Text("Cancel")
                                }
                            },
                            title = { Text("Remove ${workoutToDelete?.name.orEmpty().ifEmpty { "workout" }}?") },
                        )
                    }

                        LazyColumn(modifier = modifier
                            .background(MaterialTheme.colorScheme.background)
                        ) {
                            stickyHeader {
                                Column(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(12.dp)
                                ) {
                                    FitnessJournalButton(
                                        text = "Add Workout",
                                        onClick = {
                                            navController.navigate(
                                                FitnessJournalScreen.NewWorkoutScreen.route
                                            )
                                        },
                                        fullWidth = true
                                    )
                                }
                            }

                            items(workouts) { item ->
                                if (item != null) {
                                    WorkoutSummaryCard(
                                        item,
                                        onTap = {
                                            navController.navigate(
                                                "${FitnessJournalScreen.WorkoutDetails.route}/${
                                                    item.addedAt
                                                        .toInstant()
                                                        .toEpochMilli()
                                                }"
                                            )
                                        },
                                        onLongPress = { workoutToDelete = item }
                                    )
                                }
                            }
                        }
//                } else {
//                    EmptyWorkoutList(
//                        modifier = modifier,
//                        addWorkout = {
//                            navController.navigate(
//                                FitnessJournalScreen.NewWorkoutScreen.route
//                            )
//                        }
//                    )
//                }
//            }
//            is DataState.Error -> {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(MaterialTheme.colorScheme.background),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Text(workouts.e.message ?: "unknown error")
//                }
//            }
//            is DataState.NotSent -> {}
//        }
}

@Preview
@Composable
fun WorkoutsScreenPreview() {
    WorkoutsScreen(
        navController = rememberNavController()
    )
}
