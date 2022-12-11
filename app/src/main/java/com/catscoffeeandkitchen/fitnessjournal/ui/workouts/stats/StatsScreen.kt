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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.components.graphs.MPExerciseLineGraph
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.OffsetDateTime


@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedExercise = viewModel.selectedExerciseRequest.collectAsState(initial = null as String?)

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
            is DataState.Success -> {
                selectedExercise.value?.let { name ->
                    item {
                        Text(name, style = MaterialTheme.typography.headlineMedium)
                    }
                }

                item {
                    val statsData = state.data
                        .groupBy { it.completedAt }
                        .map { grouping ->
                            val sortedGroup = grouping.value.sortedByDescending { set ->
                                set.weightInPounds / (1.0278 - 0.0278 * set.reps)
                            }
                            StatsData(
                                date = grouping.key ?: OffsetDateTime.now(),
                                repMax = (sortedGroup.first().weightInPounds / (1.0278 - 0.0278 * sortedGroup.first().reps)).toFloat(),
                                totalVolume = grouping.value.maxOf { it.weightInPounds * it.reps },
                                highestWeight = grouping.value.maxOf { it.weightInPounds },
                                reps = grouping.value.maxOf { it.reps }.toFloat()
                            )
                        }

                    Timber.d("*** statsData = ${statsData.joinToString { it.repMax.toString() }}")

                    if (statsData.isNotEmpty()) {
                        MPExerciseLineGraph(entries = statsData)
                    } else {
                        Text("No sets to graph.")
                    }
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
                        modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                        style = MaterialTheme.typography.headlineMedium)

                    Text(
                        "completed ${exercise.amountOfSets} times",
                        modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

        }
    }

}
