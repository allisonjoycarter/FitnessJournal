package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString

@Composable
fun PlateCalculator(
    weight: Double,
    unit: WeightUnit
    ) {
    var showPlateDialog by remember { mutableStateOf(false) }
    var settings by remember {
        mutableStateOf(PlateCalculatorHelper.PlateSettings(amounts = mapOf(), unit = unit))
    }

    val plateHelper = PlateCalculatorHelper()
    var plates = plateHelper.calculatePlates(weight, settings)

    if (showPlateDialog) {
        PlateDialog(
            plateSettings = if (settings.amounts.isEmpty())
                settings.copy(amounts = plates.amounts.mapValues { entry ->
                    entry.value.takeIf { value -> value >= 0 } ?: 0 }
                )
            else settings,
            onDismissRequest = { showPlateDialog = false },
            updatePlateSettings = { updatedSettings ->
                settings = updatedSettings
                plates = plateHelper.calculatePlates(weight, updatedSettings)
            }
        )
    }

    Column() {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlateStackVertical(plates = plates.amounts)
            FitnessJournalButton(text = "change plates", onClick = { showPlateDialog = true })
        }

        if (plates.leftoverWeight > 0) {
            Card(
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text("${(plates.leftoverWeight).toCleanString()} more " +
                        "${unit.toAbbreviation()} on each side needed to reach this weight.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
