package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.ExerciseSetInput
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import java.time.OffsetDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditSetGrid(
    set: ExerciseSet,
    unit: WeightUnit,
    updateValue: (field: ExerciseSetField) -> Unit = { },
    removeSet: () -> Unit = {},
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    val setColor = when {
        set.isComplete -> MaterialTheme.colorScheme.background
        set.type == ExerciseSetType.WarmUp -> MaterialTheme.colorScheme.secondary.copy(alpha = .6f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = .6f)
    }

    val labelColor = when {
        set.isComplete -> MaterialTheme.colorScheme.onBackground
        set.type == ExerciseSetType.WarmUp -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onPrimary
    }

    var showOptionsMenu by remember { mutableStateOf(false) }

    Box() {
        ExerciseSetDropdownMenu(
            set = set,
            isVisible = showOptionsMenu,
            onDismiss = { showOptionsMenu = false },
            removeSet = removeSet,
            updateValue = {  field ->
                updateValue(field)
                showOptionsMenu = false
            }
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(setColor)
                .padding(6.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        showOptionsMenu = true
                    }
                ),
            mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
            crossAxisSpacing = 8.dp,
            mainAxisSpacing = 10.dp,
            crossAxisAlignment = FlowCrossAxisAlignment.Center
        ) {
            Checkbox(
                checked = set.isComplete,
                onCheckedChange = { checked ->
                    updateValue(
                        ExerciseSetField.Complete(if (checked) OffsetDateTime.now() else null),
                    )
                },
                colors = CheckboxDefaults.colors(
                    uncheckedColor = labelColor
                )
            )

            ExerciseSetInputWithLabel(
                value = set.reps.toString(),
                label = "reps",
                updateValue = { value ->
                    updateValue(ExerciseSetField.Reps(value.toIntOrNull() ?: 0))
                },
                labelColor = labelColor,
                onFocus = onFocus,
                onBlur = onBlur,
            )

            ExerciseSetInputWithLabel(
                value = if (unit == WeightUnit.Pounds)
                    set.weightInPounds.toCleanString() else
                    set.weightInKilograms.toCleanString(),
                label = if (unit == WeightUnit.Pounds) "lbs" else "kg",
                updateValue = { value ->
                    if (unit == WeightUnit.Pounds) {
                        updateValue(ExerciseSetField.WeightInPounds(value.toFloatOrNull() ?: 0f))
                    } else {
                        updateValue(ExerciseSetField.WeightInKilograms(value.toFloatOrNull() ?: 0f))
                    }
                },
                labelColor = labelColor,
                onFocus = onFocus,
                onBlur = onBlur,
            )

            ExerciseSetInputWithLabel(
                value = set.repsInReserve.toString(),
                label = "RIR",
                updateValue = { value ->
                    updateValue(ExerciseSetField.RepsInReserve(value.toIntOrNull() ?: 0))
                },
                labelColor = labelColor,
                onFocus = onFocus,
                onBlur = onBlur,
            )

            ExerciseSetInputWithLabel(
                value = set.perceivedExertion.toString(),
                label = "PE",
                updateValue = { value ->
                    updateValue(ExerciseSetField.PerceivedExertion(value.toIntOrNull() ?: 0))
                },
                labelColor = labelColor,
                onFocus = onFocus,
                onBlur = onBlur,
            )
        }
    }
}

@Composable
fun ExerciseSetInputWithLabel(
    value: String,
    label: String,
    labelColor: Color,
    updateValue: (value: String) -> Unit,
    onFocus: () -> Unit,
    onBlur: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ExerciseSetInput(
            startingText = value,
            onValueChange = { value ->
                updateValue(value)
            },
            onFocus = onFocus,
            onBlur = onBlur,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor
        )
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
                weightInPounds = 140f,
                repsInReserve = 3,
                perceivedExertion = 7,
                isComplete = true
            ),
            unit = WeightUnit.Pounds,
        )
    }
}
