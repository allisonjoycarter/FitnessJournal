package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.ExerciseSetModifier
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import java.time.OffsetDateTime


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.editableExerciseCardContent(
    uiData: ExerciseUiData,
    uiActions: ExerciseUiActions?,
    onCompleteExercise: (OffsetDateTime?) -> Unit,
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    val expectedSet = uiData.entry.expectedSet
    if (expectedSet != null) {
        Text(
            "${expectedSet.sets}x${expectedSet.minReps} - " +
                    "${expectedSet.maxReps}reps@${expectedSet.perceivedExertion}PE, " +
                    "${expectedSet.rir}RIR",
            modifier = Modifier.padding(4.dp)
        )
    }

    if (uiData.entry.sets.any { it.modifier == ExerciseSetModifier.SingleSide }) {
        Surface(
            modifier = Modifier.padding(vertical = 4.dp),
            shape = SuggestionChipDefaults.shape,
            tonalElevation = 4.dp
        ) {
            Text("Single Side", modifier = Modifier.padding(2.dp))
        }
    }

    Column(modifier = Modifier.animateContentSize()) {
        uiData.entry.sets.sortedBy { it.setNumber }.forEach { set ->
            SetDetailsInputs(
                set,
                useKeyboard = uiData.useKeyboard,
                updateValue = { field ->
                    when (field) {
                        is ExerciseSetField.Complete -> {
                            onCompleteExercise(field.value as OffsetDateTime?)

                            uiActions?.updateSet(
                                field.copySetWithNewValue(set)
                            )
                        }
                        is ExerciseSetField.Reps,
                        is ExerciseSetField.WeightInKilograms,
                        is ExerciseSetField.WeightInPounds -> {
                            val setsToPropagateUpdatesTo = uiData.entry.sets.filter { item ->
                                item.setNumber >= set.setNumber
                            }

                            uiActions?.updateSets(setsToPropagateUpdatesTo.map { item ->
                                field.copySetWithNewValue(item)
                            })
                        }
                        else -> {
                            uiActions?.updateSet(
                                field.copySetWithNewValue(set)
                            )
                        }
                    }
                },
                removeSet = {
                    uiActions?.removeSet(set.id)
                },
                onFocus = {
                    onFocus()
                },
                onBlur = onBlur,
                unit = uiData.unit,
            )
        }
    }

    FitnessJournalButton(
        text = "Add Set",
        onClick = { uiActions?.addExerciseSet(uiData.entry, uiData.workoutAddedAt) },
        fullWidth = true
    )
}
