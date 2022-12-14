package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.foundation.layout.*
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
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.bicepCurlSets
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.exerciseBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.expectedSetBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString


@Composable
fun ColumnScope.readOnlyExerciseCardContent(
    uiData: ExerciseUiData,
) {
    if (uiData.expectedSet != null) {
        Text(
            "${uiData.expectedSet.sets}x${uiData.expectedSet.minReps} - " +
                    "${uiData.expectedSet.maxReps}" +
                    if (uiData.expectedSet.perceivedExertion > 0 || uiData.expectedSet.rir > 0)
                        "${uiData.expectedSet.perceivedExertion}PE, ${uiData.expectedSet.rir}RIR"
                    else "",
            modifier = Modifier.padding(4.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }

    val items = uiData.sets
        .filter { it.type == ExerciseSetType.Working }
        .distinctBy { set ->
            "${set.reps}${
                if (uiData.unit == WeightUnit.Pounds)
                    set.weightInPounds else set.weightInKilograms
            }"
        }

    Column(
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items.forEach { set ->
            Row(
                modifier = Modifier.padding(start = 8.dp).width(240.dp)
            ) {
                Text(
                    "${
                        uiData.sets.count { counting ->
                            counting.reps == set.reps &&
                                    (counting.weightInPounds == set.weightInPounds ||
                                            counting.weightInKilograms == set.weightInKilograms)
                        }
                    }x${set.reps}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    if (uiData.unit == WeightUnit.Pounds) "${set.weightInPounds.toCleanString()}lbs" else
                        "${set.weightInKilograms.toCleanString()}kg",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )


                if (set.perceivedExertion > 0) {
                    Text(
                        "${set.perceivedExertion}PE",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text("", modifier = Modifier.weight(1f))
                }

                if (set.repsInReserve > 0) {
                    Text(
                        "${set.repsInReserve}RIR",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text("", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}