package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.bicepCurlSets
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.exerciseBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.expectedSetBicepCurl


@Composable
fun ReadOnlyExerciseCard(
    exercise: Exercise,
    sets: List<ExerciseSet>,
    expectedSet: ExpectedSet?,
    onEdit: () -> Unit = {}
) {
    var showExtrasDropdown by remember { mutableStateOf(false) }

    FitnessJournalCard(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(exercise.name, style = MaterialTheme.typography.headlineSmall)

            Box(modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = { showExtrasDropdown = !showExtrasDropdown },
                ) {
                    Icon(Icons.Default.MoreVert, "more exercise options")
                }

                DropdownMenu(
                    expanded = showExtrasDropdown,
                    onDismissRequest = { showExtrasDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("edit") },
                        onClick = {
                            onEdit()
                        })
                }
            }
        }

        if (expectedSet != null) {
            Text(
                "${expectedSet.sets}x${expectedSet.minReps} - " +
                        "${expectedSet.maxReps}reps@${expectedSet.perceivedExertion}PE, " +
                        "${expectedSet.rir}RIR",
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.labelLarge
            )
            Divider()
        }

        sets
            .distinctBy { it.reps }
            .distinctBy { it.weightInPounds }
            .forEach { set ->
            Text(
                "${sets.count { counting ->
                    counting.reps == set.reps &&
                    counting.weightInPounds == set.weightInPounds
                }}x${set.reps}reps" +
                        "@${set.weightInPounds}lbs, " +
                        "${set.perceivedExertion}PE, " +
                        "${set.repsInReserve}RIR",
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

    }
}

@Preview
@Composable
fun ReadOnlyExerciseCardPreview() {
    ReadOnlyExerciseCard(
        exercise = exerciseBicepCurl,
        sets = bicepCurlSets,
        expectedSet = expectedSetBicepCurl
    )
}