package com.catscoffeeandkitchen.fitnessjournal.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val nextPlan = viewModel.nextWorkoutPlan.collectAsState()
    LazyColumn(
        modifier = modifier.testTag(TestTags.ScrollableComponent),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HomeCard {
                NextWorkoutCardContent(navController = navController, state = nextPlan.value)
            }
        }


        item {
            HomeCard {
                val lastExercises = viewModel.lastExercisesCompleted.collectAsState()
                LastExercisesCardContent(state = lastExercises.value)
            }
        }

        item {
            HomeCard() {
                val dates = viewModel.weekStats.collectAsState()
                AverageWeekCardContent(
                    state = dates.value,
                )
            }
        }

        item {
            HomeCard(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                val exerciseStats = viewModel.mostImprovedExercise.collectAsState()
                MostImprovedExerciseCardContent(state = exerciseStats.value)
            }
        }
    }
}

@Composable
fun HomeCard(
    modifier: Modifier = Modifier,
    content: @Composable() ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Box(
            Modifier
                .padding(8.dp)
                .fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}