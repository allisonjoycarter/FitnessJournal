package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import java.time.Duration
import java.time.OffsetDateTime

@Composable
fun ExerciseGroupCard(
    group: ExerciseGroup,
    onExerciseSelected: (Exercise) -> Unit = {},
    editGroup: () -> Unit
) {
    var showExtrasDropdown by remember { mutableStateOf(false) }

    FitnessJournalCard(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(group.name.ifEmpty { "Select from group" },
                style = MaterialTheme.typography.headlineSmall)

            Box(modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = { showExtrasDropdown = !showExtrasDropdown },
                ) {
                    Icon(Icons.Default.MoreVert, "more exercise options")
                }
            }

            DropdownMenu(
                expanded = showExtrasDropdown,
                onDismissRequest = { showExtrasDropdown = false }
            ) {
                DropdownMenuItem(
                    text = { Text("edit group") },
                    onClick = {
                        editGroup()
                    })
            }
        }

        Column(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            group.exercises.forEach { exercise ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(8.dp)
                            .clickable { onExerciseSelected(exercise) }
                    ) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                        if (exercise.stats != null) {
                            val completed = exercise.stats?.lastCompletedAt
                            val completedAmount = exercise.stats?.amountCompleted
                            val completedAmountThisWeek = exercise.stats?.amountCompletedThisWeek

                            if (completed != null) {
                                val formattedDate = when {
                                    Duration.between(completed, OffsetDateTime.now()).toDays() < 2 -> DateUtils.getRelativeTimeSpanString(
                                        completed.toInstant().toEpochMilli(),
                                        OffsetDateTime.now().toInstant().toEpochMilli(),
                                        DateUtils.DAY_IN_MILLIS
                                    ).toString().lowercase()
                                    else -> completed.dayOfWeek.name.lowercase()
                                }
                                Text(
                                    "last completed $formattedDate",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            } else {
                                Text(
                                    "Never completed",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            if (completedAmountThisWeek != null && completedAmountThisWeek > 0) {
                                Text("$completedAmountThisWeek " +
                                        "${if (completedAmountThisWeek == 1) "set" else "sets"} " +
                                        "this week",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            } else if (completedAmount != null && completedAmount > 0) {
                                Text(
                                    "completed ${exercise.stats?.amountCompleted} sets total, none this week",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            Text("No stats available.")
                        }
                    }
                }
        }

    }
}
