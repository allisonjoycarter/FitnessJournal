package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import com.google.accompanist.flowlayout.FlowColumn

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WeightInput(
    unit: WeightUnit,
    weight: Float,
    onUpdate: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .height(75.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(6.dp)
    ) {
        val label = if (unit == WeightUnit.Kilograms) " kg" else " lbs"

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            if (weight >= 10) {
                SuggestionChip(
                    onClick = { onUpdate(weight - 10) },
                    label = { Text((weight - 10).toCleanString() + label) }
                )
            }

            if (weight >= 5) {
                SuggestionChip(
                    onClick = { onUpdate(weight - 5) },
                    label = { Text((weight - 5).toCleanString() + label) }
                )
            }

            if (unit == WeightUnit.Kilograms) {
                if (weight >= 2.5f) {
                    SuggestionChip(
                        onClick = { onUpdate(weight - 2.5f) },
                        label = { Text((weight - 2.5f).toCleanString() + label) }
                    )
                }

                SuggestionChip(
                    onClick = { onUpdate(weight + 2.5f) },
                    label = { Text((weight + 2.5f).toCleanString() + label) }
                )
            }

            SuggestionChip(
                onClick = { onUpdate(weight + 5) },
                label = { Text((weight + 5).toCleanString() + label) }
            )

            SuggestionChip(
                onClick = { onUpdate(weight + 10) },
                label = { Text((weight + 10).toCleanString() + label) }
            )
        }
    }

}