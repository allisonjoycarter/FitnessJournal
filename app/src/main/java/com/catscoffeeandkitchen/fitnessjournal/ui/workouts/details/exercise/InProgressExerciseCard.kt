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
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.bicepCurlSets
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.exerciseBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.expectedSetBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates.PlateCalculator
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import java.time.OffsetDateTime


@Composable
fun InProgressExerciseCard(
    uiData: ExerciseUiData,
    uiActions: ExerciseUiActions?,
    navigableActions: ExerciseNavigableActions?,
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    var showRemoveExerciseDialog by remember { mutableStateOf(false) }
    var timeSinceKey by remember { mutableStateOf(uiData.sets.lastOrNull { it.completedAt != null }?.completedAt) }

    if (showRemoveExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveExerciseDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    uiActions?.removeExercise(uiData.exercise)
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
            addWarmupSets = { uiActions?.addWarmupSets(
                uiData.workoutAddedAt,
                uiData.exercise,
                uiData.sets,
                uiData.unit
            ) },
            remove = { showRemoveExerciseDialog = true },
            swapExercise = { navigableActions?.swapExercise(
                uiData.exercise
            ) },
            moveUp = if (uiData.isFirstExercise) null else ({
                val position = (uiData.exercise.positionInWorkout ?: 2) - 1
                uiActions?.moveExerciseTo(uiData.exercise, position)
            }),
            moveDown = if (uiData.isLastExercise) null else ({
                val position = (uiData.exercise.positionInWorkout ?: 0) + 1
                uiActions?.moveExerciseTo(uiData.exercise, position)
            })
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
            EditSetGrid(
                completedSet,
                useKeyboard = uiData.useKeyboard,
                updateValue = { field ->
                    if (field is ExerciseSetField.Complete) {
                        timeSinceKey = if (field.value != null) {
                            field.value as OffsetDateTime
                        } else {
                            uiData.sets.sortedBy { it.setNumber }.lastOrNull { set ->
                                set.setNumber != completedSet.setNumber &&
                                        set.completedAt != null }?.completedAt
                        }
                    }
                    uiActions?.updateSet(
                        field.copySetWithNewValue(completedSet)
                    )
                },
                removeSet = {
                    uiActions?.removeSet(completedSet.id)
                },
                onFocus = {
                    onFocus()
                },
                onBlur = onBlur,
                unit = uiData.unit
            )
        }
        FitnessJournalButton(
            text = "Add Set",
            onClick = { uiActions?.addExerciseSet(uiData.exercise.name, uiData.workoutAddedAt) },
            fullWidth = true
        )
    }
}

@Preview
@Composable
fun CurrentExerciseCardPreview() {
    InProgressExerciseCard(
        ExerciseUiData(
            workoutAddedAt = OffsetDateTime.now(),
            exercise = exerciseBicepCurl,
            sets = bicepCurlSets,
            expectedSet = expectedSetBicepCurl,
            unit = WeightUnit.Pounds
        ),
        null,
        null,
    )
}
