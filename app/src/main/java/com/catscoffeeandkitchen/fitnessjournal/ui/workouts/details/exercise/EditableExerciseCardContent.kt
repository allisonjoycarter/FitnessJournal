package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetModifier
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import java.time.OffsetDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.editableExerciseCardContent(
    uiData: ExerciseUiData,
    uiActions: ExerciseUiActions?,
    onCompleteExercise: (OffsetDateTime?) -> Unit,
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    if (uiData.expectedSet != null) {
        Text(
            "${uiData.expectedSet.sets}x${uiData.expectedSet.minReps} - " +
                    "${uiData.expectedSet.maxReps}reps@${uiData.expectedSet.perceivedExertion}PE, " +
                    "${uiData.expectedSet.rir}RIR",
            modifier = Modifier.padding(4.dp)
        )
    }

    if (uiData.sets.any { it.modifier == ExerciseSetModifier.SingleLimb }) {
        Surface(
            modifier = Modifier.padding(vertical = 4.dp),
            shape = SuggestionChipDefaults.shape,
            tonalElevation = 4.dp
        ) {
            Text("Single Arm/Leg", modifier = Modifier.padding(2.dp))
        }
    }
//    Row() {
//        FilterChip(
//            selected = exerciseModifier == ExerciseSetModifier.SingleLimb,
//            onClick = {
//                if (exerciseModifier == ExerciseSetModifier.SingleLimb) {
//                    exerciseModifier = null
//                    uiData.sets.forEach { set ->
//                        uiActions?.updateSet(set.copy(modifier = null))
//                    }
//                } else {
//                    exerciseModifier = ExerciseSetModifier.SingleLimb
//                    uiData.sets.forEach { set ->
//                        uiActions?.updateSet(set.copy(modifier = ExerciseSetModifier.SingleLimb))
//                    }
//                }
//            },
//            label = { Text("Isolation") }
//        )
//    }

    uiData.sets.forEach { completedSet ->
        SetDetailsInputs(
            completedSet,
            useKeyboard = uiData.useKeyboard,
            updateValue = { field ->
                if (field is ExerciseSetField.Complete) {
                    onCompleteExercise(field.value as OffsetDateTime?)
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
