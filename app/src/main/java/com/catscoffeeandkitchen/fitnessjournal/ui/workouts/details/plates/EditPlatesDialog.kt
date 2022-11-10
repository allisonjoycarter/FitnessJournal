package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.plates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.flowlayout.FlowRow


@Composable
fun PlateDialog(
    plateSettings: PlateCalculatorHelper.PlateSettings,
    onDismissRequest: () -> Unit = {},
    updatePlateSettings: (PlateCalculatorHelper.PlateSettings) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismissRequest) {
        PlateDialogContent(plateSettings = plateSettings) { amount, plateWeight ->
            val updatedRequest = when (plateWeight) {
                45.0 -> plateSettings.copy(plates45 = amount)
                35.0 -> plateSettings.copy(plates35 = amount)
                25.0 -> plateSettings.copy(plates25 = amount)
                10.0 -> plateSettings.copy(plates10 = amount)
                5.0 -> plateSettings.copy(plates5 = amount)
                2.5 -> plateSettings.copy(plates2 = amount)
                else -> plateSettings
            }
            updatePlateSettings(updatedRequest)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateEditColumn(
    plateAmount: Int = 100,
    plateWeight: Double,
    onUpdate: (Int) -> Unit = {}
) {
    var weightInput by remember { mutableStateOf(plateAmount.toString()) }

    Column(
//        modifier = Modifier.width(150.dp)
    ) {
        TextField(
            value = weightInput,
            onValueChange = { weightInput = it },
            modifier = Modifier
                .onFocusChanged { state ->
                    if (!state.hasFocus && weightInput != plateWeight.toString()) {
                        onUpdate(weightInput.toIntOrNull() ?: 0)
                    }
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onUpdate(weightInput.toIntOrNull() ?: 0) }
            ),
            label = { Text("${plateWeight.toString().replace(".0", "")}s available") }
        )
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
            "Available Plate Amounts",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.headlineMedium
        )
        FlowRow(
            mainAxisSpacing = 18.dp,
            crossAxisSpacing = 18.dp,
            modifier = Modifier.padding(12.dp)
        ) {
            PlateEditColumn(
                plateAmount = plateSettings.plates45,
                plateWeight = 45.0
            ) { updatePlateAmount(it, 45.0) }

            PlateEditColumn(
                plateAmount = plateSettings.plates35,
                plateWeight = 35.0
            ) { updatePlateAmount(it, 35.0) }

            PlateEditColumn(
                plateAmount = plateSettings.plates25,
                plateWeight = 25.0
            ) { updatePlateAmount(it, 25.0) }

            PlateEditColumn(
                plateAmount = plateSettings.plates10,
                plateWeight = 10.0
            ) { updatePlateAmount(it, 10.0) }

            PlateEditColumn(
                plateAmount = plateSettings.plates5,
                plateWeight = 5.0
            ) { updatePlateAmount(it, 5.0) }

            PlateEditColumn(
                plateAmount = plateSettings.plates2,
                plateWeight = 2.5
            ) { updatePlateAmount(it, 2.5) }
        }
    }
}

