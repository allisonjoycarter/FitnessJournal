package com.catscoffeeandkitchen.fitnessjournal.ui.home

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.WorkoutEntry
import com.catscoffeeandkitchen.domain.util.DataState
import kotlin.math.roundToInt

@Composable
fun ColumnScope.LastExercisesCardContent(
    state: DataState<List<WorkoutEntry>>
) {
    Text("Last Exercises", style = MaterialTheme.typography.titleMedium)

    when (state) {
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