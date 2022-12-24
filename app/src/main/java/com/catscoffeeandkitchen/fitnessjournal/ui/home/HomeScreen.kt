package com.catscoffeeandkitchen.fitnessjournal.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.util.toHalf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.domain.util.capitalizeWords
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val nextPlan = viewModel.nextWorkoutPlan.collectAsState()
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HomeCard {
                when (val state = nextPlan.value) {
                    is DataState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is DataState.Error -> {
                        Text("Error = ${state.e.message}")
                    }
                    is DataState.Success -> {
                        if (state.data != null) {
                            Text(
                                "Next Workout",
                                style = MaterialTheme.typography.titleMedium,
                            )

                            Text(state.data!!.name, style = MaterialTheme.typography.titleLarge)
                            if (!state.data?.note.isNullOrEmpty()) {
                                Text(state.data!!.note!!, style = MaterialTheme.typography.bodyMedium)
                            }

                            state.data?.entries?.forEach { entry ->
                                Text("${entry.sets}x${entry.reps} " +
                                        "${entry.exerciseGroup?.name ?: entry.exercise?.name}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            TextButton(
                                onClick = {
                                    navController.navigate("${FitnessJournalScreen.WorkoutDetails.route}/0?plan=${state.data?.id}")
                                },
                                modifier = Modifier
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    "start workout",
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text("Start Workout")
                            }
                        } else {
                            Text("No workout planned.")
                        }
                    }
                    else -> {}
                }
            }
        }


        item {
            HomeCard {
                Text("Last Exercises", style = MaterialTheme.typography.titleMedium)
                val lastExercises = viewModel.lastExercisesCompleted.collectAsState()

                when (val state = lastExercises.value) {
                    is DataState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is DataState.Error -> {
                        Text("Error = ${state.e.message}")
                    }
                    is DataState.Success -> {
                        state.data.forEach { entry ->
                            Text(
                                entry.exercise?.name ?: "",
                                style = MaterialTheme.typography.labelMedium
                            )

                            Text(
                                "${entry.sets.size}x${entry.sets.map { it.reps }.average().roundToInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }
        }

        item {
            HomeCard() {
                val dates = viewModel.workoutDates.collectAsState()

                when (val state = dates.value) {
                    is DataState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is DataState.Error -> {
                        Text("Error = ${state.e.message}")
                    }
                    is DataState.Success -> {
                        Text("Average Week", style = MaterialTheme.typography.titleMedium)
                        val weekOfYear = WeekFields.of(Locale.getDefault()).weekOfYear()
                        val groupedItems = state.data
                            .groupBy { it.get(weekOfYear) }

                        val averagePerWeek = groupedItems
                            .map { it.value.size }
                            .average()
                        Text("${(averagePerWeek * 10).roundToInt() / 10.0} average workouts per week")
                        Text(
                            "over 6 months",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 2.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            val mostCommonDays = state.data
                                .groupBy { it.dayOfWeek }
                                .map { it.key to it.value.size }
                                .sortedByDescending { it.second }
                                .take(averagePerWeek.roundToInt())

                            DayOfWeek.values().filter { day -> mostCommonDays.any { it.first == day } }.forEach { day ->
                                Surface(
                                    modifier = Modifier
                                        .width(45.dp),
                                    shape = MaterialTheme.shapes.small,
                                    tonalElevation = 6.dp,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) { Text(
                                    when (day) {
                                        DayOfWeek.THURSDAY, DayOfWeek.SUNDAY -> day.name.take(2).capitalizeWords() ?: ""
                                        else -> day.name.first().toString()
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )}
                            }
                        }

                        Text(
                            "most common days",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    else -> {}
                }

            }
        }

        item {
            HomeCard(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text("Most improved exercise", style = MaterialTheme.typography.titleMedium)

                val exerciseStats = viewModel.mostImprovedExercise.collectAsState()

                when (val state = exerciseStats.value) {
                    is DataState.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    is DataState.Error -> {
                        Text("Error = ${state.e.message}")
                    }
                    is DataState.Success -> {
                        val stats = state.data
                        if (stats != null) {
                            Text("over ${stats.amountOfTime.toDays() / 7} weeks", style = MaterialTheme.typography.labelMedium)
                            Text(stats.exercise.name, style = MaterialTheme.typography.titleLarge)

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.trending_up),
                                    "Trending Up",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(end = 8.dp)
                                )
                                Text(
                                    "${(stats.ending1RM - stats.starting1RM).roundToInt()}lbs (calculated 1RM)",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                "${stats.bestSet.reps} reps " +
                                    "@ ${stats.bestSet.weightInPounds.toCleanString()}lbs",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Text(
                                stats.bestSet.completedAt?.format(DateTimeFormatter.ofPattern("MMM dd yyyy")) ?: "",
                                style = MaterialTheme.typography.labelMedium
                            )

                            Text("${stats.worstSet.reps} reps " +
                                    "@ ${stats.worstSet.weightInPounds.toCleanString()}lbs",
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Text(
                                stats.worstSet.completedAt?.format(DateTimeFormatter.ofPattern("MMM dd yyyy")) ?: "",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                    }
                    else -> {}
                }
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