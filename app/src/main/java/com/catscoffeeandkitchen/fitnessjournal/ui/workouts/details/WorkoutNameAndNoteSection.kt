package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutNameAndNoteSection(
    workoutName: String,
    workoutNote: String?,
    updateName: (String) -> Unit = {},
    updateNote: (String?) -> Unit = {},
) {
    var isEditing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isEditing) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                FitnessJournalOutlinedTextField(
                    value = workoutName,
                    onUpdate = { updateName(it) },
                    singleLine = true,
                    label = "workout name",
                )

                IconButton(
                    onClick = {
                        isEditing = false
                    },
                ) {
                    Icon(Icons.Default.Check, "stop editing name and note")
                }
            }
            FitnessJournalOutlinedTextField(
                value = workoutNote.orEmpty(),
                onUpdate = { updateNote(it.ifEmpty { null }) },
                singleLine = false,
                label = "note"
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    workoutName,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.headlineLarge
                )

                IconButton(
                    onClick = { isEditing = true },
                ) {
                    Icon(Icons.Default.Edit, "edit name and note")
                }
            }

            if (workoutNote.isNullOrEmpty()) {
                Text(
                    "No Note",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                Text(
                    workoutNote,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }

//        FitnessJournalButton(text = "${if (isEditing) "stop editing" else "edit"} name/note", onClick = { isEditing = !isEditing })
    }
}