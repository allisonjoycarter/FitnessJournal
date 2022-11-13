package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ExerciseHeaderAndDropdownMenu(
    name: String,
    addWarmupSets: () -> Unit,
    remove: () -> Unit,
    swapExercise: () -> Unit
) {
    var showExtrasDropdown by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.headlineSmall)

        Box(modifier = Modifier.weight(1f)) {
            IconButton(
                onClick = { showExtrasDropdown = !showExtrasDropdown },
            ) {
                Icon(Icons.Default.MoreVert, "more exercise options")
            }

            DropdownMenu(
                expanded = showExtrasDropdown,
                onDismissRequest = { showExtrasDropdown = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Fireplace, "create warm up sets") },
                    text = { Text("add pyramid warmup") },
                    onClick = {
                        addWarmupSets()
                    })
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Delete, "remove exercise") },
                    text = { Text("remove") },
                    onClick = {
                        remove()
                    })
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Refresh, "swap exercise") },
                    text = { Text("swap") },
                    onClick = {
                        swapExercise()
                    })
            }
        }
    }
}