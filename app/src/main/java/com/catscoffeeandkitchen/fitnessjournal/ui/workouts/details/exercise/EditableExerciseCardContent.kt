package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import java.time.OffsetDateTime


@Composable
fun ColumnScope.editableExerciseCardContent(
    uiData: ExerciseUiData,
    uiActions: ExerciseUiActions?,
    onCompleteExercise: () -> Unit,
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    var timeSinceKey by remember { mutableStateOf(uiData.sets.lastOrNull { it.completedAt != null }?.completedAt) }

    if (uiData.expectedSet != null) {
        Text(
            "${uiData.expectedSet.sets}x${uiData.expectedSet.minReps} - " +
                    "${uiData.expectedSet.maxReps}reps@${uiData.expectedSet.perceivedExertion}PE, " +
                    "${uiData.expectedSet.rir}RIR",
            modifier = Modifier.padding(4.dp)
        )
    }

    AnimatedVisibility(
        uiData.sets.any { it.isComplete } && timeSinceKey != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        TimeSinceText(
            startTime = timeSinceKey ?: OffsetDateTime.now(),
            modifier = Modifier.padding(2.dp)
        )
    }

    uiData.sets.forEach { completedSet ->
        SetDetailsInputs(
            completedSet,
            useKeyboard = uiData.useKeyboard,
            updateValue = { field ->
                if (field is ExerciseSetField.Complete) {
                    timeSinceKey = if (field.value != null) {
                        field.value as OffsetDateTime
                    } else {
                        uiData.sets.sortedBy { it.setNumber }.lastOrNull { set ->
                            set.setNumber != completedSet.setNumber &&
                                    set.completedAt != null }?.completedAt
                    }

                    val unfinishedSets = uiData.sets.filter { !it.isComplete }
                    if (unfinishedSets.size == 1 &&
                        unfinishedSets.all { it.setNumber == completedSet.setNumber } &&
                        field.value != null) {
                        onCompleteExercise()
                    }
                }
                uiActions?.updateSet(
                    field.copySetWithNewValue(completedSet)
                )
            },
            removeSet = {
                uiActions?.removeSet(completedSet.id)
            },
            onFocus = {
                onFocus()
            },
            onBlur = onBlur,
            unit = uiData.unit
        )
    }
    FitnessJournalButton(
        text = "Add Set",
        onClick = { uiActions?.addExerciseSet(uiData.exercise!!.name, uiData.workoutAddedAt) },
        fullWidth = true
    )
}
