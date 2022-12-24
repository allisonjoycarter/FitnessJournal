package com.catscoffeeandkitchen.fitnessjournal.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.WorkoutWeekStats
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.domain.util.capitalizeWords
import java.time.DayOfWeek
import java.time.OffsetDateTime
import kotlin.math.roundToInt

@Composable
fun ColumnScope.AverageWeekCardContent(
    state: DataState<WorkoutWeekStats>,
) {
    when (state) {
        is DataState.Loading -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        is DataState.Error -> {
            Text("Error = ${state.e.message}")
        }
        is DataState.Success -> {
            Text("Average Week", style = MaterialTheme.typography.titleMedium)

            val formattedWorkoutsPerWeek = (state.data.averageWorkoutsPerWeek * 10).roundToInt() / 10.0
            Text("$formattedWorkoutsPerWeek average workouts per week")
            Text(
                "over 6 months",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 2.dp)
            )

            val mostCommonHour = when {
                state.data.mostCommonTimes.first() < 13 -> "${state.data.mostCommonTimes.first()}am"
                else -> "${state.data.mostCommonTimes.first() - 12}pm"
            }
            Text("typically finishing workout around $mostCommonHour")

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                DayOfWeek.values()
                    .filter { day -> state.data.mostCommonDays.isNotEmpty() &&
                            state.data.mostCommonDays.any { it == day } }
                    .forEach { day ->
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        }
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