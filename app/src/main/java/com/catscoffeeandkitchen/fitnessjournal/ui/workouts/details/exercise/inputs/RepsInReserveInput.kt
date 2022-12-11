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
fun RepsInReserveInput(
    rir: Int,
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
                rir == 5,
                label = { Text("5 reps") },
                onClick = { onUpdate(5) }
            )

            FilterChip(
                rir == 4,
                label = { Text("4 reps") },
                onClick = { onUpdate(4) }
            )

            FilterChip(
                rir == 3,
                label = { Text("3 reps") },
                onClick = { onUpdate(3) }
            )

            FilterChip(
                rir == 2,
                label = { Text("2 reps") },
                onClick = { onUpdate(2) }
            )

            FilterChip(
                rir == 1,
                label = { Text("1 reps") },
                onClick = { onUpdate(1) }
            )

            FilterChip(
                rir == 0,
                label = { Text("0 reps") },
                onClick = { onUpdate(0) }
            )
        }
    }

}