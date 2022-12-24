@file:OptIn(ExperimentalMaterial3Api::class)

package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.ui.components.AddExerciseOrGroupButtons
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalOutlinedTextField
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import java.time.DayOfWeek

@Composable
fun WorkoutPlanEditScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutPlanViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
    val onStartOrResume by rememberUpdatedState(viewModel::addExercise)
    val groupExercises by rememberUpdatedState(viewModel::showGroupNameDialog)
    val selectGroup by rememberUpdatedState(viewModel::selectGroup)
    var exerciseToRemove by remember { mutableStateOf(null as ExpectedSet?) }
    val showExerciseGroupNameDialog = viewModel.showExerciseGroupNameDialog.collectAsState()
    val exercisesToGroup = viewModel.exercisesToGroup.collectAsState()
    var groupName by remember { mutableStateOf("") }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<String>("exerciseToAdd")?.observe(lifecycleOwner) { result ->
                        onStartOrResume(result)
                        
                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<String>("exerciseToAdd")
                    }

                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<String>("selectedExercises")?.observe(lifecycleOwner) { result ->
                        groupExercises(result.split("|"))
                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<String>("selectedExercises")
                    }

                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<Long?>("selectedGroup")?.observe(lifecycleOwner) { result ->
                        result?.let {
                            selectGroup(it)
                            navController.currentBackStackEntry?.savedStateHandle
                                ?.remove<Long>("selectedGroup")
                        }
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showExerciseGroupNameDialog.value) {
        AlertDialog(
            onDismissRequest = {
                groupName = ""
                viewModel.hideGroupNameDialog()
            },
            confirmButton = { TextButton(onClick = {
                viewModel.addExerciseGroup(exercisesToGroup.value, groupName = groupName)
                viewModel.hideGroupNameDialog()
                groupName = ""
            }) { Text("OK") }},
            dismissButton = { TextButton(onClick = {
                groupName = ""
                viewModel.addExerciseGroup(exercisesToGroup.value)
                viewModel.hideGroupNameDialog()
            }) { Text("Skip") }},
            title = { Text(
                "Name this group?",
                style = MaterialTheme.typography.titleSmall
            ) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(.9f)
                        .height(((30 * exercisesToGroup.value.size) + 120).dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        "Create a name for this group of " +
                            "exercises:\n\n${exercisesToGroup.value.joinToString("\n")}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                    )
                }
            }
        )
    }

    if (exerciseToRemove != null) {
        AlertDialog(
            onDismissRequest = { exerciseToRemove = null },
            confirmButton = { TextButton(onClick = {
                viewModel.removeSet(exerciseToRemove!!)
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
        modifier = modifier.testTag(TestTags.ScrollableComponent),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (val workoutState = viewModel.workoutPlan.value) {
            is DataState.Loading -> {
                item {
                    CircularProgressIndicator()
                }
            }
            is DataState.Success -> {
                exercisePlanItems(
                    plan = workoutState.data,
                    expectedSets = workoutState.data.entries,
                    uiActions = object : ExercisePlanUiActions {

                        override fun updateWorkoutName(name: String) {
                            viewModel.updateWorkoutName(name)
                        }

                        override fun updateWorkoutNotes(notes: String) {
                            viewModel.updateWorkoutNotes(notes)
                        }

                        override fun updateWeekdays(weekdays: List<DayOfWeek>) {
                            viewModel.updateWeekdays(weekdays)
                        }

                        override fun updateExercise(
                            setNumber: Int,
                            field: ExercisePlanField,
                            value: Int
                        ) {
                            if (field == ExercisePlanField.SetNumber) {
                                viewModel.updateExercisePosition(setNumber, value)
                            } else {
                                viewModel.updateExercisePlan(setNumber, field, value)
                            }
                        }

                        override fun startWorkout() {
                            navController.navigate(
                                "${FitnessJournalScreen.WorkoutDetails.route}/0?" +
                                        "plan=${workoutState.data.id}"
                            )
                        }

                        override fun addExercise() {
                            navController.navigate(FitnessJournalScreen.SearchExercisesScreen.route)
                        }

                        override fun addExerciseGroup() {
                            navController.navigate("${FitnessJournalScreen.ExerciseGroupScreen.route}?selectable=true")
                        }

                        override fun removeExercise(exercise: ExpectedSet) {
                            exerciseToRemove = exercise
                        }
                    }
                )
            }
            else -> {
                item {
                    Text(workoutState.toString())
                }
            }
        }
    }
}


fun LazyListScope.exercisePlanItems(
    plan: WorkoutPlan,
    expectedSets: List<ExpectedSet>,
    uiActions: ExercisePlanUiActions?
    ) {

    item {
        FitnessJournalOutlinedTextField(
            value = plan.name,
            onUpdate = { uiActions?.updateWorkoutName(it) },
            label = "plan name",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }

    item {
        FitnessJournalOutlinedTextField(
            value = plan.note.orEmpty(),
            onUpdate = { uiActions?.updateWorkoutNotes(it) },
            singleLine = false,
            label = "note",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }

    item {
        WeekRow(
            plan.daysOfWeek,
        ) { day ->
            uiActions?.updateWeekdays(
                if (plan.daysOfWeek.contains(day)) {
                    plan.daysOfWeek.filter { it != day }
                } else {
                    plan.daysOfWeek + listOf(day)
                }
            )
        }
    }

    items(expectedSets) { expected ->
        ExercisePlanCard(
            expected,
            updateExercise = { field, value ->
                uiActions?.updateExercise(expected.positionInWorkout, field, value)
            },
            removeExpectedSet = { uiActions?.removeExercise(expected) },
            isFirstSet = expectedSets.minOfOrNull { it.positionInWorkout } == expected.positionInWorkout,
            isLastSet = expectedSets.maxOfOrNull { it.positionInWorkout } == expected.positionInWorkout
        )
    }
    item {
        AddExerciseOrGroupButtons(
            addExercise = { uiActions?.addExercise() },
            addGroup = { uiActions?.addExerciseGroup() }
        )
    }

    item {
        FitnessJournalButton(
            "Start Workout",
            onClick = {
                uiActions?.startWorkout()
            },
            fullWidth = true
        )
    }
}

