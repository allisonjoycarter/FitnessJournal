package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.components.graphs.MPExerciseLineGraph
import kotlinx.coroutines.launch
import java.time.OffsetDateTime


@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Stats", style = MaterialTheme.typography.headlineLarge)
        }

        when (val state = viewModel.sets.value) {
            is DataState.NotSent -> item { Text("Select an exercise!") }
            is DataState.Loading -> item { CircularProgressIndicator() }
            is DataState.Error -> item { Text(state.e.message.toString()) }
            is DataState.Success -> item {
                val pairs = state.data
                    .groupBy { it.completedAt }
                    .map { grouping ->
                        val sortedGroup = grouping.value.sortedByDescending { set ->
                            set.weightInPounds / ( 1.0278 - 0.0278 * set.reps)
                        }
                        Pair(
                            (grouping.key ?: OffsetDateTime.now()),
                            (sortedGroup.first().weightInPounds / ( 1.0278 - 0.0278 * sortedGroup.first().reps )).toFloat())
                    }

                if (pairs.isNotEmpty()) {
                    MPExerciseLineGraph(entries = pairs)
                } else {
                    Text("No sets to graph.")
                }
            }
        }

        when (val state = viewModel.exercises.value) {
            is DataState.NotSent -> item { Text("Not getting exercises at the moment.") }
            is DataState.Loading -> item { CircularProgressIndicator() }
            is DataState.Error -> item { Text(state.e.message.toString()) }
            is DataState.Success -> items(state.data) {exercise ->
                FitnessJournalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable {
                            viewModel.selectExercise(exercise.name)

                            coroutineScope.launch {
                                listState.scrollToItem(0)
                            }
                        }
                ) {
                    Text(
                        exercise.name,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.headlineMedium)
                }
            }

        }
    }

}
