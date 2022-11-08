package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.bicepCurlSets
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.exerciseBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.util.PreviewConstants.expectedSetBicepCurl
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.ExerciseModifierViewModel
import timber.log.Timber


@Composable
fun CurrentExerciseCard(
    exercise: Exercise,
    sets: List<ExerciseSet>,
    expectedSet: ExpectedSet?,
    useKeyboardForEntry: Boolean,
    addSet: (Exercise) -> Unit = {},
    addWarmupSets: (Exercise) -> Unit = {},
    removeSet: (set: ExerciseSet) -> Unit = { },
    removeExercise: (exercise: Exercise) -> Unit = { },
    updateExercise: (ExerciseSet, field: ExerciseSetField, value: Int) -> Unit = { _,_,_ ->},
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
                    removeExercise(exercise)
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
            title = { Text("Remove this exercise and ${sets.size} sets?") },
        )
    }

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
                        leadingIcon = { Icon(Icons.Default.Fireplace, "create warm up sets")},
                        text = { Text("add pyramid warmup") },
                        onClick = {
                            addWarmupSets(exercise)
                        })
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Delete, "remove exercise")},
                        text = { Text("remove") },
                        onClick = {
                            showRemoveExerciseDialog = true
                        })
                }
            }
        }

        if (expectedSet != null) {
            Text(
                "${expectedSet.sets}x${expectedSet.minReps} - " +
                        "${expectedSet.maxReps}reps@${expectedSet.perceivedExertion}PE, " +
                        "${expectedSet.rir}RIR",
                modifier = Modifier.padding(4.dp)
            )
        }

        sets.forEach { completedSet ->
            Divider(modifier = Modifier.padding(bottom = 2.dp))
            EditSetGrid(
                completedSet,
                useKeyboard = useKeyboardForEntry,
                updateValue = { field, value ->
                    updateExercise(completedSet, field, value)
                },
                removeSet = { setToRemove = completedSet },
                onFocus = {
                    onFocus()
                },
                onBlur = onBlur,
            )

            if (!completedSet.isComplete &&
                completedSet.setNumberInWorkout == sets.first { set ->
                    set.exercise.name == completedSet.exercise.name && !set.isComplete }
                    .setNumberInWorkout ) {
                PlateCalculator(weight = completedSet.weightInPounds.toDouble())
            }
        }
        FitnessJournalButton(text = "Add Set", onClick = { addSet(exercise) }, fullWidth = true)
    }
}

@Preview
@Composable
fun CurrentExerciseCardPreview() {
    CurrentExerciseCard(
        exercise = exerciseBicepCurl,
        sets = bicepCurlSets,
        expectedSet = expectedSetBicepCurl,
        useKeyboardForEntry = false
    )
}
