package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.bicepCurlSets
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.exerciseBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.expectedSetBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates.PlateCalculator
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import timber.log.Timber


@Composable
fun CurrentExerciseCard(
    uiData: ExerciseUiData,
    addSet: (Exercise) -> Unit = {},
    addWarmupSets: (Exercise) -> Unit = {},
    removeSet: (set: ExerciseSet) -> Unit = { },
    removeExercise: (exercise: Exercise) -> Unit = { },
    updateExercise: (ExerciseSet, field: ExerciseSetField) -> Unit = { _, _ ->},
    swapExercise: () -> Unit = { },
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    var setToRemove by remember { mutableStateOf(null as ExerciseSet?) }
    var showRemoveExerciseDialog by remember { mutableStateOf(false) }

    if (setToRemove != null) {
        AlertDialog(
            shape = MaterialTheme.shapes.small,
            onDismissRequest = { setToRemove = null },
            confirmButton = {
                TextButton(onClick = {
                    setToRemove?.let { removeSet(it) }
                    setToRemove = null
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { setToRemove = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Remove this set?") },
        )
    }

    if (showRemoveExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveExerciseDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    removeExercise(uiData.exercise)
                    showRemoveExerciseDialog = false
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveExerciseDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Remove this exercise and ${uiData.sets.size} sets?") },
        )
    }

    var showExtrasDropdown by remember { mutableStateOf(false) }

    FitnessJournalCard(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(uiData.exercise.name, style = MaterialTheme.typography.headlineSmall)

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
                        leadingIcon = { Icon(Icons.Default.Fireplace, "create warm up sets")},
                        text = { Text("add pyramid warmup") },
                        onClick = {
                            addWarmupSets(uiData.exercise)
                        })
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Delete, "remove exercise")},
                        text = { Text("remove") },
                        onClick = {
                            showRemoveExerciseDialog = true
                        })
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Refresh, "swap exercise")},
                        text = { Text("swap") },
                        onClick = {
                            swapExercise()
                        })
                }
            }
        }

        if (uiData.expectedSet != null) {
            Text(
                "${uiData.expectedSet.sets}x${uiData.expectedSet.minReps} - " +
                        "${uiData.expectedSet.maxReps}reps@${uiData.expectedSet.perceivedExertion}PE, " +
                        "${uiData.expectedSet.rir}RIR",
                modifier = Modifier.padding(4.dp)
            )
        }

        uiData.sets.forEach { completedSet ->
            Divider(modifier = Modifier.padding(bottom = 2.dp))
            EditSetGrid(
                completedSet,
                updateValue = { field ->
                    updateExercise(completedSet, field)
                },
                removeSet = { setToRemove = completedSet },
                onFocus = {
                    onFocus()
                },
                onBlur = onBlur,
                unit = uiData.unit
            )

            Timber.d("${uiData.exercise.name} = ${completedSet.exercise.equipmentType.name}")
            if (!completedSet.isComplete &&
                completedSet.exercise.equipmentType == ExerciseEquipmentType.Barbell &&
                completedSet.setNumberInWorkout == uiData.sets.first { set ->
                    !set.isComplete && set.exercise.name == completedSet.exercise.name }
                    .setNumberInWorkout ) {
                PlateCalculator(weight = if (uiData.unit == WeightUnit.Pounds)
                    completedSet.weightInPounds.toDouble() else 
                        completedSet.weightInKilograms.toDouble(),
                    unit = uiData.unit,
                )
            }
        }
        FitnessJournalButton(text = "Add Set", onClick = { addSet(uiData.exercise) }, fullWidth = true)
    }
}

@Preview
@Composable
fun CurrentExerciseCardPreview() {
    CurrentExerciseCard(ExerciseUiData(
        exercise = exerciseBicepCurl,
        sets = bicepCurlSets,
        expectedSet = expectedSetBicepCurl,
        unit = WeightUnit.Pounds
    ))
}
