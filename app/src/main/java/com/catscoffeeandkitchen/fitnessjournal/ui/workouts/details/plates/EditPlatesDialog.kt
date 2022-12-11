package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.catscoffeeandkitchen.fitnessjournal.R


@Composable
fun PlateDialog(
    plateSettings: PlateCalculatorHelper.PlateSettings,
    onDismissRequest: () -> Unit = {},
    updatePlateSettings: (PlateCalculatorHelper.PlateSettings) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismissRequest) {
        PlateDialogContent(plateSettings = plateSettings) { amount, plateWeight ->
            val updatedAmounts = plateSettings.amounts.toMutableMap()
            updatedAmounts[plateWeight] = amount

            updatePlateSettings(plateSettings.copy(amounts = updatedAmounts))
        }
    }
}

@Composable
fun PlateEditColumn(
    plateAmount: Int = 100,
    plateWeight: Double,
    onUpdate: (Int) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            plateWeight.toString() + "s",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 12.dp)
            )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                enabled = plateAmount > 0,
                onClick = { onUpdate(plateAmount - 1) }
            ) {
                Icon(painterResource(R.drawable.remove), "remove plate")
            }

            Text(
                plateAmount.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(6.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface)
            )

            IconButton(onClick = { onUpdate(plateAmount + 1) }) {
                Icon(Icons.Default.Add, "add plate")
            }
        }
    }
}


@Composable
fun PlateDialogContent(
    plateSettings: PlateCalculatorHelper.PlateSettings,
    updatePlateAmount: (amount: Int, weight: Double) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Text(
            "Plates to Use",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.headlineMedium
        )

        plateSettings.amounts.entries.forEach { entry ->
            PlateEditColumn(
                plateAmount = entry.value,
                plateWeight = entry.key
            ) { updatePlateAmount(it, entry.key) }
        }
    }
}

