package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton

@Composable
fun PlateCalculator(
    weight: Double,
    viewModel: PlateCalculatorViewModel = hiltViewModel()
) {
    val plates = viewModel.plates.collectAsState(initial = mapOf<Int, Double>())

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        plates.value.filter { it.value.toDouble() > 0 }.forEach { entry ->
            Text("${entry.value}x${entry.key.toString().replace(".0", "")}s",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge)
        }

        FitnessJournalButton(text = "calculate plates", onClick = {
            viewModel.requestWeight(PlateCalculatorViewModel.PlateRequest(weight = weight))
        })
    }
}

@Preview
@Composable
fun PlateCalculatorPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PlateCalculator(weight = 135.0)
        PlateCalculator(weight = 235.0)
        PlateCalculator(weight = 255.0)
        PlateCalculator(weight = 275.0)
    }
}