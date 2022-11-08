package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.ExerciseSetInput
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import timber.log.Timber

@Composable
fun EditSetGrid(
    set: ExerciseSet,
    useKeyboard: Boolean,
    updateValue: (field: ExerciseSetField, value: Int) -> Unit = {_, _, -> },
    removeSet: () -> Unit = {},
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    val setColor = when {
        set.isComplete -> MaterialTheme.colorScheme.background
        set.type == ExerciseSetType.WarmUp -> MaterialTheme.colorScheme.secondary.copy(alpha = .3f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = .3f)
    }

    var showOptionsMenu by remember { mutableStateOf(false) }

    Box() {
        DropdownMenu(expanded = showOptionsMenu, onDismissRequest = { showOptionsMenu = false }) {
            when (set.type) {
                ExerciseSetType.Working -> {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Fireplace, "change to warm up set")},
                        text = { Text("make warmup set") },
                        onClick = {
                            updateValue(ExerciseSetField.Type, ExerciseSetType.WarmUp.ordinal)
                        },
                    )
                }
                ExerciseSetType.WarmUp -> {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.LocalFireDepartment, "change to working set")},
                        text = {
                            Text("make working set") },
                        onClick = {
                            updateValue(ExerciseSetField.Type, ExerciseSetType.Working.ordinal)
                        },
                    )
                }
            }

            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.Delete, "remove set")},
                text = { Text("remove") },
                onClick = { removeSet() })

        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(setColor)
                .padding(6.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showOptionsMenu = true
                        }
                    )
                }
            ,
            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
            crossAxisSpacing = 8.dp,
            mainAxisSpacing = 10.dp,
        ) {
            Checkbox(
                checked = set.isComplete,
                onCheckedChange = { checked ->
                    updateValue(
                        ExerciseSetField.Complete,
                        if (checked) 1 else 0
                    )
                }
            )

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExerciseSetInput(
                    startingText = set.reps.toString(),
                    onValueChange = { value ->
                        updateValue(ExerciseSetField.Reps, value.toIntOrNull() ?: 0)
                    },
                    onFocus = onFocus,
                    onBlur = onBlur,
                    useKeyboard = useKeyboard
                )
                Text("reps", style = MaterialTheme.typography.labelLarge)
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExerciseSetInput(
                    startingText = set.weightInPounds.toString(),
                    onValueChange = { value ->
                        updateValue(ExerciseSetField.WeightInPounds, value.toIntOrNull() ?: 0)
                    },
                    onFocus = onFocus,
                    onBlur = onBlur,
                    useKeyboard = useKeyboard
                )
                Text(
                    "lbs",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExerciseSetInput(
                    startingText = set.repsInReserve.toString(),
                    onValueChange = { value ->
                        updateValue(ExerciseSetField.RepsInReserve, value.toIntOrNull() ?: 0)
                    },
                    onFocus = onFocus,
                    onBlur = onBlur,
                    useKeyboard = useKeyboard
                )
                Text("RIR", style = MaterialTheme.typography.labelLarge)
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExerciseSetInput(
                    startingText = set.perceivedExertion.toString(),
                    onValueChange = { value ->
                        updateValue(ExerciseSetField.PerceivedExertion, value.toIntOrNull() ?: 0)
                    },
                    onFocus = onFocus,
                    onBlur = onBlur,
                    useKeyboard = useKeyboard
                )
                Text("PE", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(
    name = "Grid"
)
@Composable
fun SetItemGridPreview() {
    Card {
        EditSetGrid(
            set = ExerciseSet(
                id = 0L,
                exercise = Exercise(
                    name = "Bicep Curl",
                    musclesWorked = listOf()
                ),
                reps = 4,
                setNumberInWorkout = 1,
                weightInPounds = 140,
                repsInReserve = 3,
                perceivedExertion = 7,
                isComplete = true
            ),
            useKeyboard = false
        )
    }
}
