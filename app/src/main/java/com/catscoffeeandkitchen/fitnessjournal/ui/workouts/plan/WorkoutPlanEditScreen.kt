@file:OptIn(ExperimentalMaterial3Api::class)

package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalOutlinedTextField
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import timber.log.Timber

@Composable
fun WorkoutPlanEditScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutPlanViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
    val onStartOrResume by rememberUpdatedState(viewModel::addExercise)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<String>("exerciseToAdd")?.observe(lifecycleOwner) { result ->
                        Timber.d("*** running onStartOrResume for $result")
                        val parts = result.split("|")
                        onStartOrResume(
                            parts.first(),
                        )
                        
                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<String>("exerciseToAdd")
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (val workoutState = viewModel.workoutPlan.value) {
        is DataState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        is DataState.Success -> {
            ExercisePlanColumn(
                modifier = modifier,
                plan = workoutState.data,
                updateWorkoutName = { name ->
                    viewModel.updateWorkoutName(name)
                },
                updateWorkoutNotes = { notes ->
                    viewModel.updateWorkoutNotes(notes)
                },
                expectedSets = workoutState.data.exercises,
                updateExercise = { setNumber, field, value ->
                    viewModel.updateExercisePlan(setNumber, field, value)
                },
                addExercise = {
                    navController.navigate(FitnessJournalScreen.SearchExercisesScreen.route)
                },
                removeExercise = { expected ->
                    viewModel.removeSet(expected)
                }
            )
        }
        else -> { Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(workoutState.toString())
        } }
    }
}


@Composable
fun ExercisePlanColumn(
    plan: WorkoutPlan,
    expectedSets: List<ExpectedSet>,
    modifier: Modifier = Modifier,
    updateWorkoutName: (String) -> Unit = {},
    updateWorkoutNotes: (String) -> Unit = {},
    updateExercise: (setNumber: Int, field: ExercisePlanField, value: Int) -> Unit = { _, _, _ -> },
    addExercise: () -> Unit = {},
    removeExercise: (expectedSet: ExpectedSet) -> Unit = {}
    ) {
    var exerciseToRemove by remember { mutableStateOf(null as ExpectedSet?) }

    var title by remember { mutableStateOf(plan.name)}
    var note by remember { mutableStateOf(plan.note.orEmpty())}

    if (exerciseToRemove != null) {
        AlertDialog(
            onDismissRequest = { exerciseToRemove = null },
            confirmButton = { TextButton(onClick = {
                removeExercise(exerciseToRemove!!)
                exerciseToRemove = null
            }) { Text("Remove") }},
            dismissButton = { TextButton(onClick = {
                exerciseToRemove = null
            }) { Text("Cancel") }},
            title = { Text(
                "Remove ${exerciseToRemove?.exercise?.name ?: "exercise"}?",
                style = MaterialTheme.typography.titleSmall
            ) },
        )
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FitnessJournalOutlinedTextField(
                value = title,
                onUpdate = { updateWorkoutName(it) },
                label = "plan name",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }

        item {
            FitnessJournalOutlinedTextField(
                value = note,
                onUpdate = { updateWorkoutNotes(it) },
                singleLine = false,
                label = "note",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }

        items(expectedSets) { expected ->
            ExercisePlanCard(
                expected,
                updateExercise = { field, value ->
                    updateExercise(expected.setNumberInWorkout, field, value)
                },
                removeExpectedSet = { exerciseToRemove = expected }
            )
        }
        item {
            FitnessJournalButton(
                "Add Exercise",
                onClick = {
                    addExercise()
                },
                fullWidth = true
            )
        }
    }
}

@Composable
fun ExercisePlanCard(
    expectedSet: ExpectedSet,
    updateExercise: (field: ExercisePlanField, value: Int) -> Unit = { _, _ -> },
    removeExpectedSet: () -> Unit = {}
) {
    FitnessJournalCard(
        columnPaddingVertical = 4.dp,
        columnPaddingHorizontal = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        removeExpectedSet()
                    }
                )
            }
    ) {
        Text(
            expectedSet.exercise.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            expectedSet.exercise.musclesWorked.joinToString(", "),
            style = MaterialTheme.typography.labelSmall
        )

        EditExercisePlanGrid(
            expectedSet,
            updateValue = { field, value ->
                updateExercise(field, value)
            },
        )
    }
}
