package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.fitnessjournal.R

@Composable
fun ExerciseSetDropdownMenu(
    set: ExerciseSet,
    isVisible: Boolean,
    onDismiss: () -> Unit = {},
    removeSet: () -> Unit = {},
    showPlateCalculator: () -> Unit = {},
    updateValue: (ExerciseSetField) -> Unit
) {
    DropdownMenu(expanded = isVisible, onDismissRequest = { onDismiss() }) {
        when (set.type) {
            ExerciseSetType.Working -> {
                DropdownMenuItem(
                    leadingIcon = { Icon(painterResource(id = R.drawable.fireplace), "change to warm up set") },
                    text = { Text("make warmup set") },
                    onClick = {
                        updateValue(ExerciseSetField.Type(ExerciseSetType.WarmUp))
                        onDismiss()
                    },
                )
            }
            ExerciseSetType.WarmUp -> {
                DropdownMenuItem(
                    leadingIcon = { Icon(painterResource(R.drawable.whatshot), "change to working set") },
                    text = {
                        Text("make working set") },
                    onClick = {
                        updateValue(ExerciseSetField.Type(ExerciseSetType.Working))
                        onDismiss()
                    },
                )
            }
        }

        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.Delete, "remove set") },
            text = { Text("remove") },
            onClick = {
                removeSet()
                onDismiss()
            })

        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.Info, "show plates") },
            text = { Text("show plate calculator") },
            onClick = {
                showPlateCalculator()
                onDismiss()
            })

    }

}