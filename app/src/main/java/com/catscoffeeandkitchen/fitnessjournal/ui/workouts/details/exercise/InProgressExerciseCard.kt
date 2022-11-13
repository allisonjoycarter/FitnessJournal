package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.bicepCurlSets
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.exerciseBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.expectedSetBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates.PlateCalculator
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.EditSetGrid
import java.time.OffsetDateTime


@Composable
fun InProgressExerciseCard(
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
    var timeSinceKey by remember { mutableStateOf(uiData.sets.lastOrNull { it.completedAt != null }?.completedAt) }

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

    FitnessJournalCard(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        ExerciseHeaderAndDropdownMenu(
            name = uiData.exercise.name,
            addWarmupSets = { addWarmupSets(uiData.exercise) },
            remove = { showRemoveExerciseDialog = true },
            swapExercise = swapExercise
        )

        if (uiData.expectedSet != null) {
            Text(
                "${uiData.expectedSet.sets}x${uiData.expectedSet.minReps} - " +
                        "${uiData.expectedSet.maxReps}reps@${uiData.expectedSet.perceivedExertion}PE, " +
                        "${uiData.expectedSet.rir}RIR",
                modifier = Modifier.padding(4.dp)
            )
        }

        AnimatedVisibility(
            uiData.sets.any { it.isComplete } && timeSinceKey != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            TimeSinceText(
                startTime = timeSinceKey ?: OffsetDateTime.now(),
                modifier = Modifier.padding(2.dp)
            )
        }

        uiData.sets.forEach { completedSet ->
            Divider(modifier = Modifier.padding(bottom = 2.dp))
            EditSetGrid(
                completedSet,
                updateValue = { field ->
                    if (field is ExerciseSetField.Complete) {
                        timeSinceKey = if (field.value != null) {
                            field.value as OffsetDateTime
                        } else {
                            uiData.sets.sortedBy { it.setNumberInWorkout }.lastOrNull { set ->
                                set.setNumberInWorkout != completedSet.setNumberInWorkout &&
                                        set.completedAt != null }?.completedAt
                        }
                    }
                    updateExercise(completedSet, field)
                },
                removeSet = { setToRemove = completedSet },
                onFocus = {
                    onFocus()
                },
                onBlur = onBlur,
                unit = uiData.unit
            )

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
    InProgressExerciseCard(
        ExerciseUiData(
        exercise = exerciseBicepCurl,
        sets = bicepCurlSets,
        expectedSet = expectedSetBicepCurl,
        unit = WeightUnit.Pounds
    )
    )
}
