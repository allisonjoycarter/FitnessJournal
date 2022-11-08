package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalInputButton
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.CurrentWorkoutViewModel
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.ExerciseModifierViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputModalContent(
    viewModel: CurrentWorkoutViewModel = hiltViewModel(),
    exerciseModifierViewModel: ExerciseModifierViewModel = hiltViewModel()
) {
    InputModalButtons(
        addToValue = { field, amountToAdd ->
        },
        subtractFromValue = { field, amount ->
        },
        setValue = { field, amount ->
        }
    )

    ShowKeyboardButton()
}

@Composable
fun InputModalButtons(
    addToValue: (ExerciseSetField, Int) -> Unit = { _, _ ->},
    subtractFromValue: (ExerciseSetField, Int) -> Unit = { _, _ -> },
    setValue: (ExerciseSetField, Int) -> Unit = { _, _ -> },
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            FitnessJournalInputButton(
                text = "-2 reps",
                onClick = {
                    subtractFromValue(ExerciseSetField.Reps, 2)
                })
            FitnessJournalInputButton(
                text = "+2 reps",
                onClick = {
                    addToValue(ExerciseSetField.Reps, 2)
                })
            FitnessJournalInputButton(
                text = "10 reps",
                onClick = {
                    setValue(ExerciseSetField.Reps, 10)
                })
        }

        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            FitnessJournalInputButton(
                text = "+5lbs",
                onClick = {
                    addToValue(ExerciseSetField.WeightInPounds, 5)
                })
            FitnessJournalInputButton(
                text = "+10lbs",
                onClick = {
                    addToValue(ExerciseSetField.WeightInPounds, 10)
                })
            FitnessJournalInputButton(
                text = "+45lbs",
                onClick = {
                    addToValue(ExerciseSetField.WeightInPounds, 45)
                })
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowKeyboardButton() {
    val keyboardController = LocalSoftwareKeyboardController.current
    FitnessJournalInputButton(
        text = "Show System Keyboard",
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        onClick = {
            keyboardController?.show()
        }
    )
}

@Preview(
    showBackground = true,
    widthDp = 300
)
@Composable
fun InputModalContentPreview(
) {
    Column(
    ) {
        InputModalButtons()
        ShowKeyboardButton()
    }
}