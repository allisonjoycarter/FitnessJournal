package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard

@Composable
fun ExercisePlanCard(
    expectedSet: ExpectedSet,
    updateExercise: (field: ExercisePlanField, value: Int) -> Unit = { _, _ -> },
    removeExpectedSet: () -> Unit = {},
    isFirstSet: Boolean = false,
    isLastSet: Boolean = false,
) {
    FitnessJournalCard(
        columnPaddingVertical = 4.dp,
        columnPaddingHorizontal = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        removeExpectedSet()
                    }
                )
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val name: String = when {
                expectedSet.exercise != null -> expectedSet.exercise!!.name
                expectedSet.exerciseGroup?.name != null -> expectedSet.exerciseGroup!!.name
                expectedSet.exerciseGroup != null -> expectedSet.exerciseGroup!!.exercises.joinToString { it.name }
                else -> "Exercise"
            }
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )

            IconButton(
                onClick = { updateExercise(ExercisePlanField.SetNumber, expectedSet.positionInWorkout - 1) },
                enabled = !isFirstSet,
                modifier = Modifier.weight(.5f)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, "move exercise up")
            }

            IconButton(
                onClick = { updateExercise(ExercisePlanField.SetNumber, expectedSet.positionInWorkout + 1) },
                enabled = !isLastSet,
                modifier = Modifier.weight(.5f),
            ) {
                Icon(Icons.Default.KeyboardArrowDown, "move exercise down")
            }
        }

        if (expectedSet.exercise != null) {
            Text(
                expectedSet.exercise?.musclesWorked?.joinToString(", ").orEmpty(),
                style = MaterialTheme.typography.labelSmall
            )
        } else {
            Text(
                expectedSet.exerciseGroup?.exercises?.joinToString { it.name } ?: "Group",
                style = MaterialTheme.typography.labelSmall
            )
        }

        EditExercisePlanGrid(
            expectedSet,
            updateValue = { field, value ->
                updateExercise(field, value)
            },
        )
    }
}
