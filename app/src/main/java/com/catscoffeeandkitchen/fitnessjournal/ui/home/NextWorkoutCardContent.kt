package com.catscoffeeandkitchen.fitnessjournal.ui.home

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen

@Composable
fun ColumnScope.NextWorkoutCardContent(
    navController: NavController,
    state: DataState<WorkoutPlan?>
) {
    when (state) {
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