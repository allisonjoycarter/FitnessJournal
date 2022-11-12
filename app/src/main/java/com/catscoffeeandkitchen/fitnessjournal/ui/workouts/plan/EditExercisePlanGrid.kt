package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun EditExercisePlanGrid(
    expectedSet: ExpectedSet,
    updateValue: (field: ExercisePlanField, value: Int) -> Unit = { _, _, -> },
) {
    FlowRow(
        modifier = Modifier.padding(12.dp).fillMaxWidth(),
        mainAxisSpacing = 12.dp,
        crossAxisSpacing = 8.dp
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExerciseSetInput(
                startingText = expectedSet.sets.toString(),
                onValueChange = { value ->
                    if (value.toIntOrNull() != null) {
                        updateValue(ExercisePlanField.Sets, value.toIntOrNull() ?: 0)
                    }
                },
                onFocus = {},
                onBlur = {},
            )
            Text("sets", style = MaterialTheme.typography.labelLarge)
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExerciseSetInput(
                startingText = expectedSet.reps.toString(),
                onValueChange = { value ->
                    if (value.toIntOrNull() != null) {
                        updateValue(ExercisePlanField.Reps, value.toIntOrNull() ?: 0)
                    }
                },
                onFocus = {},
                onBlur = {},
            )
            Text("reps", style = MaterialTheme.typography.labelLarge)
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExerciseSetInput(
                startingText = expectedSet.minReps.toString(),
                onValueChange = { value ->
                    if (value.toIntOrNull() != null) {
                        updateValue(ExercisePlanField.MinReps, value.toIntOrNull() ?: 0)
                    }
                },
                onFocus = {},
                onBlur = {},
            )
            Text("min reps", style = MaterialTheme.typography.labelLarge)
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExerciseSetInput(
                startingText = expectedSet.maxReps.toString(),
                onValueChange = { value ->
                    if (value.toIntOrNull() != null) {
                        updateValue(ExercisePlanField.MaxReps, value.toIntOrNull() ?: 0)
                    }
                },
                onFocus = {},
                onBlur = {},
            )
            Text("max reps", style = MaterialTheme.typography.labelLarge)
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExerciseSetInput(
                startingText = expectedSet.rir.toString(),
                onValueChange = { value ->
                    if (value.toIntOrNull() != null) {
                        updateValue(ExercisePlanField.RepsInReserve, value.toIntOrNull() ?: 0)
                    }
                },
                onFocus = { },
                onBlur = { },
            )
            Text("reps in reserve", style = MaterialTheme.typography.labelLarge)
        }
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ExerciseSetInput(
                startingText = expectedSet.perceivedExertion.toString(),
                onValueChange = { value ->
                    if (value.toIntOrNull() != null) {
                        updateValue(ExercisePlanField.PerceivedExertion, value.toIntOrNull() ?: 0)
                    }
                },
                onFocus = {},
                onBlur = {},
            )
            Text("perceived exertion", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExerciseSetInput(
    startingText: String,
    onValueChange: (value: String) -> Unit,
    onFocus: () -> Unit,
    onBlur: () -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(startingText)) }
    var focusTextKey by remember { mutableStateOf("")  }

    BasicTextField(
        value = textFieldValue,
        singleLine = true,
        onValueChange = { newText ->
            textFieldValue = newText
        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier
            .requiredWidth(60.dp)
            .onFocusChanged { state ->
                if (state.hasFocus) {
                    onFocus()
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                    focusTextKey = startingText
                } else {
                    if (focusTextKey == startingText) {
                        onBlur()
                        if (textFieldValue.text.isNotEmpty()) {
                            onValueChange(textFieldValue.text)
                        }
                    }
                    focusTextKey = ""
                }
            },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(onDone = {
            onValueChange(textFieldValue.text)
        }),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
            ) {
                innerTextField()
            }
        }
    )
}

@Preview(
    name = "Grid"
)
@Composable
fun SetItemGridPreview() {
    Card {
        EditExercisePlanGrid(
            expectedSet = ExpectedSet(
                Exercise(
                    "Bicep Curls",
                    listOf("Biceps"),
                ),
                reps = 8,
                minReps = 6,
                maxReps = 10,
                perceivedExertion = 8,
                rir = 3
            ),
        )
    }
}
