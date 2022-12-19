package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.ExerciseSetModifier
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import timber.log.Timber
import java.time.OffsetDateTime

enum class ExerciseCardState {
    Group,
    Exercise,
    FinishedExercise,
    Invalid
}

@Composable
fun ExerciseCard(
    uiData: ExerciseUiData,
    uiActions: ExerciseUiActions?,
    navigableActions: ExerciseNavigableActions?,
    onCompleteSet: (OffsetDateTime?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingExercise by remember { mutableStateOf(null as String?) }
    val exercise = uiData.entry.exercise
    val individualSets = uiData.entry.sets.sortedBy { it.setNumber }
    val exerciseIsFinished = exercise != null &&
            editingExercise != exercise.name &&
            individualSets.all { it.isComplete }
    val expectedGroup = uiData.entry.expectedSet?.exerciseGroup

    FitnessJournalCard(
        modifier = modifier.padding(horizontal = 8.dp),
        columnItemSpacing = 0.dp,
    ) {
        when {
            exerciseIsFinished -> {
                CardHeaderRow(title = uiData.entry.exercise?.name.orEmpty()) { dismissMenu ->
                    DropdownMenuItem(
                        text = { Text("edit") },
                        onClick = {
                            editingExercise = uiData.entry.exercise?.name
                            dismissMenu()
                        })
                }

                readOnlyExerciseCardContent(uiData)
            }
            exercise != null -> {
                CardHeaderRow(title = uiData.entry.exercise?.name.orEmpty()) { dismissMenu ->
                    exerciseMenuItems(uiData, uiActions, navigableActions, dismissMenu)
                }

                editableExerciseCardContent(
                    uiData,
                    uiActions,
                    onCompleteExercise = { time ->
                        onCompleteSet(time)
                    }
                )
            }
            expectedGroup != null -> {
                CardHeaderRow(
                    title = (uiData.entry.expectedSet?.exerciseGroup?.name ?: "")
                        .ifEmpty { "Select from group" })
                { onDismiss ->
                    DropdownMenuItem(
                        text = { Text("edit group") },
                        onClick = {
                            onDismiss()
                            uiData.entry.expectedSet?.exerciseGroup?.let {
                                navigableActions?.editGroup(it) }
                        })
                }

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .testTag(TestTags.ChooseExerciseFromGroup),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiData.entry.expectedSet?.exerciseGroup?.exercises?.forEach { exercise ->
                        GroupExerciseItem(exercise, onSelect = { selected ->
                            uiActions?.selectExerciseFromGroup(
                                uiData.entry.expectedSet?.exerciseGroup!!,
                                selected,
                                uiData.entry.position, uiData.entry.expectedSet)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.exerciseMenuItems(
    uiData: ExerciseUiData,
    uiActions: ExerciseUiActions?,
    navigableActions: ExerciseNavigableActions?,
    dismissMenu: () -> Unit
) {
    DropdownMenuItem(
        leadingIcon = { Icon(painterResource(R.drawable.fireplace), "create warm up sets") },
        text = { Text("add pyramid warmup") },
        onClick = {
            uiActions?.addWarmupSets(
                uiData.workoutAddedAt,
                uiData.entry,
                uiData.unit
            )
            dismissMenu()
        })
    DropdownMenuItem(
        leadingIcon = { Icon(Icons.Default.Delete, "remove exercise") },
        text = { Text("remove") },
        onClick = {
            uiActions?.removeEntry(uiData.entry)
            dismissMenu()
        })

    DropdownMenuItem(
        leadingIcon = { Icon(Icons.Default.Refresh, "swap exercise") },
        text = { Text("swap") },
        onClick = {
            navigableActions?.swapExerciseAt(uiData.entry.position)
            dismissMenu()
        })

    if (uiData.wasChosenFromGroup) {
        DropdownMenuItem(
            leadingIcon = { Icon(painterResource(R.drawable.checklist), "show group") },
            text = { Text("choose from group") },
            onClick = {
                uiActions?.replaceWithGroup(uiData.entry)
                dismissMenu()
            })
    }

    val hasSingleSideModifier = uiData.entry.sets.all { it.modifier == ExerciseSetModifier.SingleSide }
    DropdownMenuItem(
        leadingIcon = { Icon(Icons.Default.Check, "mark as single arm/leg") },
        text = { Text("${if (hasSingleSideModifier) "un" else ""}mark as single arm/leg") },
        onClick = {
            if (hasSingleSideModifier) {
                uiActions?.updateSets(uiData.entry.sets.map { it.copy(modifier = null) })
            } else {
                uiActions?.updateSets(uiData.entry.sets.map { it.copy(modifier = ExerciseSetModifier.SingleSide) })
            }
            dismissMenu()
        })

    if (!uiData.isFirstExercise) {
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, "move up") },
            text = { Text("move up") },
            onClick = {
                uiActions?.moveEntryTo(uiData.entry, uiData.entry.position - 1)
                dismissMenu()
            })
    }

    if (!uiData.isLastExercise) {
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, "move down") },
            text = { Text("move down") },
            onClick = {
                uiActions?.moveEntryTo(uiData.entry, uiData.entry.position + 1)
                dismissMenu()
            })
    }
}

@Composable
fun CardHeaderRow(
    title: String,
    dropdownItems: @Composable() ColumnScope.(dismissMenu: () -> Unit) -> Unit,
) {
    var showExtrasDropdown by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title,
            style = MaterialTheme.typography.titleMedium)

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
            dropdownItems(dismissMenu = { showExtrasDropdown = false })
        }
    }
}
