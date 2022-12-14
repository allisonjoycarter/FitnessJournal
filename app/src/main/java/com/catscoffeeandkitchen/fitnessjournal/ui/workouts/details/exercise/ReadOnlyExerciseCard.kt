package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
fun ReadOnlyExerciseCard(
    exercise: Exercise,
    sets: List<ExerciseSet>,
    expectedSet: ExpectedSet?,
    unit: WeightUnit,
    onEdit: () -> Unit = {}
) {
    var showExtrasDropdown by remember { mutableStateOf(false) }

    FitnessJournalCard(
        modifier = Modifier.padding(horizontal = 8.dp),
        columnItemSpacing = 0.dp,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)

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
                        "${expectedSet.maxReps}" +
                        if (expectedSet.perceivedExertion > 0 || expectedSet.rir > 0)
                            "${expectedSet.perceivedExertion}PE, ${expectedSet.rir}RIR"
                        else "",
                modifier = Modifier.padding(4.dp),
                style = MaterialTheme.typography.labelLarge
            )
            Divider()
        }

        val items = sets
            .filter { it.type == ExerciseSetType.Working }
            .distinctBy { set ->
                "${set.reps}${if (unit == WeightUnit.Pounds)
                    set.weightInPounds else set.weightInKilograms}"
            }

        Column(
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items.forEach { set ->
                Row(
                    modifier = Modifier.padding(start = 8.dp).width(240.dp)
                ) {
                        Text(
                            "${sets.count { counting ->
                                counting.reps == set.reps &&
                                        (counting.weightInPounds == set.weightInPounds ||
                                                counting.weightInKilograms == set.weightInKilograms)
                            }}x${set.reps}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Text(if (unit == WeightUnit.Pounds) "${set.weightInPounds.toCleanString()}lbs" else
                            "${set.weightInKilograms.toCleanString()}kg",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )


                        if (set.perceivedExertion > 0) {
                            Text("${set.perceivedExertion}PE",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text("", modifier = Modifier.weight(1f))
                        }

                        if (set.repsInReserve > 0) {
                            Text("${set.repsInReserve}RIR",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text("", modifier = Modifier.weight(1f))
                        }
                    }
                }
        }
//
//        sets
//            .filter { it.type == ExerciseSetType.Working }
//            .distinctBy { set ->
//                "${set.reps}${if (unit == WeightUnit.Pounds)
//                    set.weightInPounds else set.weightInKilograms}"
//            }
//            .forEach { set ->
//            Text(
//                buildAnnotatedString {
//                    withStyle(
//                        style = SpanStyle(fontWeight = FontWeight.Bold)
//                    ) {
//                        append(
//                            "${
//                                sets.count { counting ->
//                                    counting.reps == set.reps &&
//                                            (counting.weightInPounds == set.weightInPounds ||
//                                                    counting.weightInKilograms == set.weightInKilograms)
//                                }
//                            }x${set.reps}\t\t"
//                        )
//                    }
//
//                    withStyle(
//                        style = SpanStyle(fontWeight = FontWeight.Bold)
//                    ) {
//                        append((if (unit == WeightUnit.Pounds) "${set.weightInPounds.toCleanString()}lbs" else
//                            "${set.weightInKilograms.toCleanString()}kg"))
//                    }
//
//                    if (set.perceivedExertion > 0) {
//                        append("\t${set.perceivedExertion}PE")
//                    }
//
//                    if (set.repsInReserve > 0) {
//                        append("\t${set.repsInReserve}RIR")
//                    }
//
//                },
//                modifier = Modifier.padding(4.dp),
//                style = MaterialTheme.typography.bodyLarge
//            )
//        }

    }
}

@Preview
@Composable
fun ReadOnlyExerciseCardPreview() {
    ReadOnlyExerciseCard(
        exercise = exerciseBicepCurl,
        sets = bicepCurlSets,
        expectedSet = expectedSetBicepCurl,
        unit = WeightUnit.Pounds
    )
}