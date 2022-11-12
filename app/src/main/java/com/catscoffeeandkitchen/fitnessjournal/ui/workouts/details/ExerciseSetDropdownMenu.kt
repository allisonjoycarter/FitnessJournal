package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType

@Composable
fun ExerciseSetDropdownMenu(
    set: ExerciseSet,
    isVisible: Boolean,
    onDismiss: () -> Unit = {},
    removeSet: () -> Unit = {},
    updateValue: (ExerciseSetField) -> Unit
) {
    DropdownMenu(expanded = isVisible, onDismissRequest = { onDismiss() }) {
        when (set.type) {
            ExerciseSetType.Working -> {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Fireplace, "change to warm up set") },
                    text = { Text("make warmup set") },
                    onClick = {
                        updateValue(ExerciseSetField.Type(ExerciseSetType.WarmUp))
                    },
                )
            }
            ExerciseSetType.WarmUp -> {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.LocalFireDepartment, "change to working set") },
                    text = {
                        Text("make working set") },
                    onClick = {
                        updateValue(ExerciseSetField.Type(ExerciseSetType.Working))
                    },
                )
            }
        }

        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.Delete, "remove set") },
            text = { Text("remove") },
            onClick = { removeSet() })

    }

}