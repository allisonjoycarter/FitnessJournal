package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
            val updatedAmounts = plateSettings.amounts.toMutableMap()
            updatedAmounts[plateWeight] = amount

            updatePlateSettings(plateSettings.copy(amounts = updatedAmounts))
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
            plateSettings.amounts.entries.forEach { entry ->
                PlateEditColumn(
                    plateAmount = entry.value,
                    plateWeight = entry.key
                ) { updatePlateAmount(it, entry.key) }
            }
        }
    }
}

