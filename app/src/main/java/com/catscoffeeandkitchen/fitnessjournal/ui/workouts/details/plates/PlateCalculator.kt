package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.plates

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import timber.log.Timber

@Composable
fun PlateCalculator(
    weight: Double,
    ) {
    var showPlateDialog by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(PlateCalculatorHelper.PlateSettings()) }

    val plateHelper = PlateCalculatorHelper()
    var plates = plateHelper.calculatePlates(weight, settings)

    if (showPlateDialog) {
        PlateDialog(
            plateSettings = settings,
            onDismissRequest = { showPlateDialog = false },
            updatePlateSettings = { updatedSettings ->
                settings = updatedSettings
                plates = plateHelper.calculatePlates(weight, updatedSettings)
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlateStackVertical(plates = plates)
        FitnessJournalButton(text = "edit plates", onClick = { showPlateDialog = true })
    }
}
