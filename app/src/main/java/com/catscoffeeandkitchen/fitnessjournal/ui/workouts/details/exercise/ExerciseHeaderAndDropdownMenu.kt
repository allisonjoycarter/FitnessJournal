package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.catscoffeeandkitchen.fitnessjournal.R

@Composable
fun ExerciseHeaderAndDropdownMenu(
    name: String,
    addWarmupSets: () -> Unit,
    remove: () -> Unit,
    swapExercise: () -> Unit,
    chooseFromGroup: (() -> Unit)? = null,
    moveUp: (() -> Unit)? = null,
    moveDown: (() -> Unit)? = null,
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
                    leadingIcon = { Icon(painterResource(R.drawable.fireplace), "create warm up sets") },
                    text = { Text("add pyramid warmup") },
                    onClick = {
                        addWarmupSets()
                        showExtrasDropdown = false
                    })
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Delete, "remove exercise") },
                    text = { Text("remove") },
                    onClick = {
                        remove()
                        showExtrasDropdown = false
                    })

                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Refresh, "swap exercise") },
                    text = { Text("swap") },
                    onClick = {
                        swapExercise()
                        showExtrasDropdown = false
                    })

                if (chooseFromGroup != null) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(painterResource(R.drawable.checklist), "show group") },
                        text = { Text("choose from group") },
                        onClick = {
                            chooseFromGroup()
                            showExtrasDropdown = false
                        })
                }

                if (moveUp != null) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, "move up") },
                        text = { Text("move up") },
                        onClick = {
                            moveUp()
                            showExtrasDropdown = false
                        })
                }

                if (moveDown != null) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, "move down") },
                        text = { Text("move down") },
                        onClick = {
                            moveDown()
                            showExtrasDropdown = false
                        })
                }
            }
        }
    }
}