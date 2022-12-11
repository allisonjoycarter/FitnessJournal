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
import com.google.accompanist.flowlayout.FlowColumn

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RepsInput(
    reps: Int,
    onUpdate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .height(100.dp)
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
                selected = reps == 3,
                onClick = { onUpdate(3) },
                label = { Text("3") }
            )

            FilterChip(
                selected = reps == 5,
                onClick = { onUpdate(5) },
                label = { Text("5") }
            )

            FilterChip(
                selected = reps == 8,
                onClick = { onUpdate(8) },
                label = { Text("8") }
            )

            FilterChip(
                selected = reps == 10,
                onClick = { onUpdate(10) },
                label = { Text("10") }
            )

            FilterChip(
                selected = reps == 12,
                onClick = { onUpdate(12) },
                label = { Text("12") }
            )

            FilterChip(
                selected = reps == 15,
                onClick = { onUpdate(15) },
                label = { Text("15") }
            )

            FilterChip(
                selected = reps == 20,
                onClick = { onUpdate(20) },
                label = { Text("20") }
            )
        }
    }

}