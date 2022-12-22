package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.list.WorkoutPlanSummaryCard
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.list.WorkoutPlansViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectPlanScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    workoutPlansViewModel: WorkoutPlansViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    when (val planState = workoutPlansViewModel.workouts.value) {
        is DataState.Loading -> { CircularProgressIndicator() }
        is DataState.Error -> { Text("Error = ${planState.e.localizedMessage}")}
        is DataState.Success -> {
            LaunchedEffect(coroutineScope) {
                if (planState.data.isEmpty()) {
                    navController.navigate("${FitnessJournalScreen.WorkoutDetails.route}/0") {
                        popUpTo(FitnessJournalScreen.WorkoutsScreen.route)
                    }
                }
            }

            LazyColumn(
                modifier = modifier.padding(12.dp).testTag(TestTags.ScrollableComponent),
            ) {
                stickyHeader {
                    Text(
                        "Select a Plan",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                items(planState.data) { item ->
                    WorkoutPlanSummaryCard(
                        workout = item,
                        onTap = {
                            navController.navigate(
                                "${FitnessJournalScreen.WorkoutDetails.route}/0?plan=${item.id}"
                            ) {
                                popUpTo(FitnessJournalScreen.WorkoutsScreen.route)
                            }
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            navController.navigate(
                                "${FitnessJournalScreen.WorkoutDetails.route}/0"
                            ) {
                                popUpTo(FitnessJournalScreen.WorkoutsScreen.route)
                            }
                        }) {
                            Text("Skip")
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@Preview
@Composable
fun SelectPlanScreenPreview() {
    SelectPlanScreen(
        navController = rememberNavController()
    )
}