package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.exercisegroups

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExerciseGroupSummaryCard(
    group: ExerciseGroup,
    renameGroup: (String) -> Unit,
    editExercises: () -> Unit,
    removeGroup: () -> Unit,
    onTap: (() -> Unit)? = null,
) {
    var showExtrasDropdown by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf(group.name) }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    renameGroup(groupName)
                    showRenameDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = { },
            title = { Text("Rename Group", style = MaterialTheme.typography.headlineMedium) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(.8f).height(50.dp)) {
                    TextField(groupName, onValueChange = { groupName = it })
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(enabled = onTap != null, onClick = { if (onTap != null) onTap() }),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(group.name.ifEmpty { "Group" },
                style = MaterialTheme.typography.headlineSmall)

            Box(modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = { showExtrasDropdown = !showExtrasDropdown },
                ) {
                    Icon(Icons.Default.MoreVert, "more group options")
                }
            }

            DropdownMenu(
                expanded = showExtrasDropdown,
                onDismissRequest = { showExtrasDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("rename") },
                    onClick = {
                        showExtrasDropdown = false
                        showRenameDialog = true
                    })

                DropdownMenuItem(
                    text = { Text("edit exercises") },
                    onClick = {
                        showExtrasDropdown = false
                        editExercises()
                    })

                DropdownMenuItem(
                    text = { Text("remove") },
                    onClick = {
                        showExtrasDropdown = false
                        removeGroup()
                    })
            }
        }

        group.exercises.forEach { exercise ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    exercise.name,
                    style = MaterialTheme.typography.headlineSmall
                )

                exercise.stats?.lastCompletedAt?.let { lastCompleted ->
                    Text(
                        "last completed ${lastCompleted.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                exercise.stats?.amountCompleted?.let { amount ->
                    Text(
                        if (amount > 0) "completed $amount times" else "never completed",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 12.dp)

                    )
                }
            }

        }
    }
}