package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.inputs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.ExerciseSetInput


@Composable
fun ExerciseSetInputWithLabel(
    value: String,
    label: String,
    labelColor: Color,
    updateValue: (value: String) -> Unit,
    onFocus: () -> Unit,
    onBlur: () -> Unit,
    focusRequester: FocusRequester? = null
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
            focusRequester = focusRequester
        )
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor
        )
    }
}
