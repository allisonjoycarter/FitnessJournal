package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.searchexercises.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.domain.util.capitalizeWords
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CreateOrChangeExerciseDialog(
    isCreating: Boolean = true,
    currentExercise: Exercise,
    onDismiss: () -> Unit = {},
    onConfirm: (Exercise) -> Unit = {},
) {
    var name by remember { mutableStateOf(currentExercise.name) }
    var category by remember { mutableStateOf(currentExercise.category) }
    var muscles by remember { mutableStateOf(currentExercise.musclesWorked) }
    var equipmentType by remember { mutableStateOf(currentExercise.equipmentType) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    )  {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("${if (isCreating) "Create" else "Update"} Exercise") },
                    navigationIcon = { IconButton(onClick = { onDismiss() }) {
                        Icon(Icons.Default.Close, "cancel creating exercise") }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )

                Text("Area Worked", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    crossAxisSpacing = 2.dp,
                    mainAxisSpacing = 6.dp
                ) {
                    listOf(
                        "Abs",
                        "Arms",
                        "Back",
                        "Calves",
                        "Cardio",
                        "Chest",
                        "Legs",
                        "Shoulders",
                        "Full Body"
                    ).forEach { cat ->
                        InputChip(
                            selected = cat == category,
                            onClick = { category = cat },
                            label = { Text(cat) },
                        )
                    }
                }

                Text("Muscles Worked", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    crossAxisSpacing = 2.dp,
                    mainAxisSpacing = 6.dp
                ) {
                    listOf(
                        "Biceps",
                        "Hamstrings",
                        "Calves",
                        "Glutes",
                        "Lats",
                        "Obliques",
                        "Pecs",
                        "Quads",
                        "Abs",
                        "Traps",
                        "Triceps",
                        "Anterior Delts",
                    ).forEach { muscle ->
                        InputChip(
                            selected = muscles.contains(muscle),
                            onClick = {
                                muscles = if (muscles.contains(muscle)) {
                                    muscles.filter { it != muscle }
                                } else {
                                    muscles + listOf(muscle)
                                }
                            },
                            label = { Text(muscle) },
                        )
                    }
                }

                Text("Exercise Type", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    crossAxisSpacing = 2.dp,
                    mainAxisSpacing = 6.dp
                ) {
                    ExerciseEquipmentType.values().forEach { type ->
                        InputChip(
                            selected = type == equipmentType,
                            onClick = { equipmentType = type },
                            label = { Text(type.name) },
                            leadingIcon = {
                                if (type == equipmentType) {
                                    Icon(Icons.Default.Check, "${type.name} selected")
                                }
                            }
                        )
                    }
                }

                FitnessJournalButton(
                    text = if (isCreating) "Create" else "Save",
                    fullWidth = true,
                    onClick = { onConfirm(
                        Exercise(
                            name = name,
                            musclesWorked = muscles,
                            category = category,
                            equipmentType = equipmentType
                        )
                    )}
                )
            }
        }
    }
}