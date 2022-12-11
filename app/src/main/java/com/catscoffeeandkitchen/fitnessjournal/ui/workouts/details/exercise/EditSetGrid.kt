package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseEquipmentType
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.inputs.*
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.plates.PlateCalculator
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import java.time.OffsetDateTime

enum class InputToDisplay() {
    Reps,
    Weight,
    RIR,
    PE,
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun EditSetGrid(
    set: ExerciseSet,
    unit: WeightUnit,
    useKeyboard: Boolean = false,
    updateValue: (field: ExerciseSetField) -> Unit = { },
    removeSet: () -> Unit = {},
    onFocus: () -> Unit = {},
    onBlur: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var setWithUpdatedData by remember { mutableStateOf(set) }
    var showInput by remember { mutableStateOf(null as InputToDisplay?) }
    var inputOpen by remember { mutableStateOf(false) }

    val animateDisappear = remember { MutableTransitionState(true) }

    val setColor = when {
        set.isComplete -> MaterialTheme.colorScheme.background
        set.type == ExerciseSetType.WarmUp -> MaterialTheme.colorScheme.secondary.copy(alpha = .6f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = .6f)
    }

    val labelColor = when {
        set.isComplete -> MaterialTheme.colorScheme.onBackground
        set.type == ExerciseSetType.WarmUp -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onPrimary
    }

    var showOptionsMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier) {
        ExerciseSetDropdownMenu(
            set = set,
            isVisible = showOptionsMenu,
            onDismiss = { showOptionsMenu = false },
            removeSet = {
                animateDisappear.targetState = false
                showOptionsMenu = false
                removeSet()
            },
            updateValue = {  field ->
                updateValue(field)
                showOptionsMenu = false
            }
        )

        AnimatedVisibility(visibleState = animateDisappear) {
            Column {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(setColor)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                showOptionsMenu = true
                            }
                        )
                        .padding(6.dp),
                    mainAxisAlignment = MainAxisAlignment.SpaceEvenly,
                    crossAxisSpacing = 8.dp,
                    mainAxisSpacing = 10.dp,
                    crossAxisAlignment = FlowCrossAxisAlignment.Center
                ) {
                    Checkbox(
                        checked = setWithUpdatedData.isComplete,
                        onCheckedChange = { checked ->
                            updateValue(
                                ExerciseSetField.Complete(if (checked) OffsetDateTime.now() else null),
                            )
                            setWithUpdatedData = setWithUpdatedData.copy(
                                completedAt = (if (checked) OffsetDateTime.now() else null),
                                isComplete = checked
                            )
                        },
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = labelColor
                        )
                    )

                    SetInput(
                        value = setWithUpdatedData.reps.toString(),
                        label = "reps",
                        labelColor = labelColor,
                        useKeyboard = useKeyboard,
                        onFocus = {
                            if (useKeyboard) {
                                showInput = InputToDisplay.Reps
                                inputOpen = true
                            } else {
                                if (!inputOpen || showInput != InputToDisplay.Reps) {
                                    keyboardController?.hide()
                                    showInput = InputToDisplay.Reps
                                    inputOpen = true
                                } else {
                                    inputOpen = false
                                }
                            }
                        },
                        updateValue = { value ->
                            updateValue(ExerciseSetField.Reps(value.toIntOrNull() ?: 0))
                            setWithUpdatedData = setWithUpdatedData.copy(reps = value.toIntOrNull() ?: 0) },
                        onBlur = {
                            showInput = null
                        }
                    )

                    SetInput(
                        value = if (unit == WeightUnit.Pounds)
                            setWithUpdatedData.weightInPounds.toCleanString() else
                            setWithUpdatedData.weightInKilograms.toCleanString(),
                        label = if (unit == WeightUnit.Pounds) "lbs" else "kg",
                        useKeyboard = useKeyboard,
                        updateValue = { value ->
                            setWithUpdatedData = if (unit == WeightUnit.Pounds) {
                                updateValue(ExerciseSetField.WeightInPounds(value.toFloatOrNull() ?: 0f))
                                setWithUpdatedData.copy(weightInPounds = value.toFloatOrNull() ?: 0f)

                            } else {
                                updateValue(ExerciseSetField.WeightInKilograms(value.toFloatOrNull() ?: 0f))
                                setWithUpdatedData.copy(weightInKilograms = value.toFloatOrNull() ?: 0f)
                            }
                        },
                        labelColor = labelColor,
                        onFocus = {
                            if (useKeyboard) {
                                showInput = InputToDisplay.Weight
                            } else {
                                if (!inputOpen || showInput != InputToDisplay.Weight) {
                                    keyboardController?.hide()
                                    inputOpen = true
                                    showInput = InputToDisplay.Weight
                                } else {
                                    inputOpen = false
                                }
                            }
                        },
                        onBlur = {
                            showInput = null
                        }
                    )

                    SetInput(
                        value = setWithUpdatedData.repsInReserve.toString(),
                        label = "RIR",
                        useKeyboard = useKeyboard,
                        updateValue = { value ->
                            updateValue(ExerciseSetField.RepsInReserve(value.toIntOrNull() ?: 0))
                            setWithUpdatedData = setWithUpdatedData.copy(repsInReserve = value.toIntOrNull() ?: 0)

                        },
                        labelColor = labelColor,
                        onFocus = {
                            if (useKeyboard) {
                                showInput = InputToDisplay.RIR
                            } else {
                                if (!inputOpen || showInput != InputToDisplay.RIR) {
                                    keyboardController?.hide()
                                    inputOpen = true
                                    showInput = InputToDisplay.RIR
                                } else {
                                    inputOpen = false
                                }
                            }
                        },
                        onBlur = onBlur,
                    )

                    SetInput(
                        value = setWithUpdatedData.perceivedExertion.toString(),
                        label = "PE",
                        useKeyboard = useKeyboard,
                        updateValue = { value ->
                            updateValue(ExerciseSetField.PerceivedExertion(value.toIntOrNull() ?: 0))
                            setWithUpdatedData = setWithUpdatedData.copy(perceivedExertion = value.toIntOrNull() ?: 0)
                        },
                        labelColor = labelColor,
                        onFocus = {
                            if (useKeyboard) {
                                showInput = InputToDisplay.PE
                            } else {
                                if (!inputOpen || showInput != InputToDisplay.PE) {
                                    keyboardController?.hide()
                                    inputOpen = true
                                    showInput = InputToDisplay.PE
                                } else {
                                    inputOpen = false
                                }
                            }
                        },
                        onBlur = onBlur,
                    )
                }

                AnimatedVisibility(
                    visible = !useKeyboard && inputOpen,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    when (showInput) {
                        InputToDisplay.Reps -> {
                            RepsInput(setWithUpdatedData.reps, onUpdate = { reps ->
                                updateValue(ExerciseSetField.Reps(reps))
                                setWithUpdatedData = setWithUpdatedData.copy(reps = reps)
                                inputOpen = false
                            })
                        }
                        InputToDisplay.RIR -> {
                            RepsInReserveInput(
                                rir = setWithUpdatedData.repsInReserve,
                                onUpdate = { rir ->
                                    updateValue(ExerciseSetField.RepsInReserve(rir))
                                    setWithUpdatedData = setWithUpdatedData.copy(repsInReserve = rir)
                                    inputOpen = false
                                })
                        }
                        InputToDisplay.Weight -> {
                            WeightInput(
                                unit,
                                if (unit == WeightUnit.Kilograms)
                                    setWithUpdatedData.weightInKilograms else setWithUpdatedData.weightInPounds,
                                onUpdate = { weight ->
                                    setWithUpdatedData = if (unit == WeightUnit.Kilograms) {
                                        updateValue(ExerciseSetField.WeightInKilograms(weight))
                                        setWithUpdatedData.copy(weightInKilograms = weight)
                                    } else {
                                        updateValue(ExerciseSetField.WeightInPounds(weight))
                                        setWithUpdatedData.copy(weightInPounds = weight)
                                    }
                                    inputOpen = false
                                }
                            )
                        }
                        InputToDisplay.PE -> {
                            PerceivedExertionInput(
                                pe = setWithUpdatedData.perceivedExertion,
                                onUpdate = { pe ->
                                    updateValue(ExerciseSetField.PerceivedExertion(pe))
                                    setWithUpdatedData = setWithUpdatedData.copy(perceivedExertion = pe)
                                    inputOpen = false
                                })
                        }
                        else -> {}
                    }
                }

                if (!setWithUpdatedData.isComplete &&
                    setWithUpdatedData.exercise.equipmentType == ExerciseEquipmentType.Barbell) {
                    PlateCalculator(weight = if (unit == WeightUnit.Pounds)
                        setWithUpdatedData.weightInPounds.toDouble() else
                        setWithUpdatedData.weightInKilograms.toDouble(),
                        unit = unit,
                    )
                }
            }
        }
    }
}

@Composable
fun SetInput(
    value: String,
    useKeyboard: Boolean,
    label: String,
    labelColor: Color,
    updateValue: (value: String) -> Unit,
    onFocus: () -> Unit,
    onBlur: () -> Unit,
) {
    if(!useKeyboard) {
        ExerciseSetButtonInput(
            value = value,
            label = label,
            labelColor = labelColor,
            onClick = onFocus,
        )
    } else {
        ExerciseSetInputWithLabel(
            value = value,
            label = label,
            updateValue = updateValue,
            labelColor = labelColor,
            onFocus = onFocus,
            onBlur = onBlur
        )
    }
}


@Preview(
    name = "Grid"
)
@Composable
fun SetItemGridPreview() {
    Card {
        EditSetGrid(
            set = ExerciseSet(
                id = 0L,
                exercise = Exercise(
                    name = "Bicep Curl",
                    musclesWorked = listOf()
                ),
                reps = 4,
                setNumber = 1,
                weightInPounds = 140f,
                repsInReserve = 3,
                perceivedExertion = 7,
                isComplete = true
            ),
            unit = WeightUnit.Pounds,
        )
    }
}
