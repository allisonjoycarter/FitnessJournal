package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.components.graphs.MPExerciseLineGraph
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedExercise = viewModel.selectedExerciseRequest.collectAsState(initial = null as String?)

    // 0 = calendar, 1 = bar chart
    var currentTab by remember { mutableStateOf(0)}
    val tabOptions = listOf("Calendar", "Charts")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = currentTab,
        ) {
            tabOptions.forEachIndexed { index, tab ->
                Tab(
                    selected = currentTab == index,
                    onClick = { currentTab = index },
                    text = { Text(text = tab) }
                )
            }
        }

        when (currentTab) {
            0 -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    val dates = viewModel.workoutDates.value

                    when (dates) {
                        is DataState.Success -> {
                            Timber.d("*** completedDates = ${dates.data.joinToString { it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }}")
                            for (monthsAgo in 0 until 3) {
                                val dateMonthsAgo = OffsetDateTime.now().withDayOfMonth(1).minusMonths(monthsAgo.toLong())
                                val lastDayOfMonth = dateMonthsAgo.plusMonths(1).withDayOfMonth(1).minusDays(1)
                                val monthInDays = Duration.between(dateMonthsAgo, lastDayOfMonth).toDays().toInt() + 1

                                item(
                                    span = { GridItemSpan(7) }
                                ) {
                                    Text(
                                        dateMonthsAgo.month.name.lowercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                items(7) { number ->
                                    val day = dateMonthsAgo.withDayOfMonth(1).with(DayOfWeek.SUNDAY).plusDays(number.toLong())
                                    Text(
                                        day.dayOfWeek.name.lowercase().take(3),
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }

                                items(monthInDays.absoluteValue) { number ->
                                    val day = dateMonthsAgo.withDayOfMonth(1).plusDays(number.toLong())
                                    val daysUntilStartOfWeek = Duration.between(
                                        dateMonthsAgo
                                            .withDayOfMonth(1)
                                            .with(DayOfWeek.SUNDAY)
                                            .plusDays(number.toLong()),
                                        day
                                    ).toDays()
                                    val dayWithCorrectWeekday = day.plusDays(daysUntilStartOfWeek - 1)

                                    val matchingDate = dates.data.find { completedDate ->
                                        dayWithCorrectWeekday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ==
                                                completedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                    }

                                    if (dayWithCorrectWeekday.month != day.month) {
                                        Text("")
                                    } else {
                                        Text(
                                            (dayWithCorrectWeekday.dayOfMonth).toString(),
                                            textAlign = TextAlign.Center,
                                            color = if (matchingDate != null)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(
                                                    if (matchingDate != null)
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.background
                                                )
                                                .padding(6.dp)
                                        )
                                    }
                                }

                                item(
                                    span = { GridItemSpan(7) }
                                ) {
                                    Divider(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp))
                                }
                            }
                        }
                        else -> { item { CircularProgressIndicator() } }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = modifier.padding(horizontal = 8.dp).testTag(TestTags.ScrollableComponent),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                            bestSet = sortedGroup.first(),
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
                        else -> {

                        }
                    }

                    item {
                        // TODO: stats recap
                        // 12 month change
                        // 6 month change
                        // 3 month change

                        // average sets performed per week
                    }

                    when (val state = viewModel.exercises.value) {
                        is DataState.NotSent -> item { Text("Not getting exercises at the moment.") }
                        is DataState.Loading -> item { CircularProgressIndicator() }
                        is DataState.Error -> item { Text(state.e.message.toString()) }
                        is DataState.Success -> items(state.data) { exercise ->
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
                                    style = MaterialTheme.typography.headlineMedium
                                )

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
        }
    }
}
