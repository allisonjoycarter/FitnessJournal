package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.list.EmptyWorkoutList
import timber.log.Timber

@Composable
fun WorkoutPlansScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutPlansViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
    val onStartOrResume by rememberUpdatedState(viewModel::getWorkouts)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                onStartOrResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Timber.d(viewModel.workouts.value.toString())
        when (val workouts = viewModel.workouts.value) {
            is DataState.Loading -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.fillMaxSize(.3f)) {
                        CircularProgressIndicator()
                    }
                }
            }
            is DataState.Success -> {
                if (workouts.data.isNotEmpty()) {
                        LazyColumn(modifier = modifier
                            .background(MaterialTheme.colorScheme.background)
                            .testTag(TestTags.ScrollableComponent),
                        ) {
                            items(workouts.data) { item ->
                                WorkoutPlanSummaryCard(
                                    item,
                                    onTap = {
                                        navController.navigate(
                                            "${FitnessJournalScreen.WorkoutPlanEditScreen.route}/${item.id}"
                                        )
                                    }
                                )
                            }
                        }
                } else {
                    EmptyWorkoutList(
                        modifier = modifier,
                    )
                }
            }
            is DataState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(workouts.e.message ?: "unknown error")
                }
            }
            is DataState.NotSent -> {}
        }
}

@Preview
@Composable
fun WorkoutPlansScreenPreview() {
    WorkoutPlansScreen(
        navController = rememberNavController()
    )
}
