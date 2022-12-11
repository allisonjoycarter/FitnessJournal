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
fun PerceivedExertionInput(
    pe: Int,
    onUpdate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .height(75.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .padding(6.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(
                pe == 6,
                label = { Text("6") },
                onClick = { onUpdate(6) }
            )

            FilterChip(
                pe == 7,
                label = { Text("7") },
                onClick = { onUpdate(7) }
            )

            FilterChip(
                pe == 8,
                label = { Text("8") },
                onClick = { onUpdate(8) }
            )

            FilterChip(
                pe == 9,
                label = { Text("9") },
                onClick = { onUpdate(9) }
            )

            FilterChip(
                pe == 10,
                label = { Text("10") },
                onClick = { onUpdate(10) }
            )

        }
    }

}