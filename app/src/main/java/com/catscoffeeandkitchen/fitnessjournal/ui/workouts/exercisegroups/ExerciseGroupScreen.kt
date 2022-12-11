package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.exercisegroups

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import timber.log.Timber


@Composable
fun ExerciseGroupScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    selectable: Boolean = false,
    viewModel: ExerciseGroupsViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
    val groupState = viewModel.exerciseGroups.collectAsState()
    val createGroup by rememberUpdatedState(viewModel::createGroup)
    val updateGroupExercises by rememberUpdatedState(viewModel::updateGroupExercises)
    var editingGroup by remember { mutableStateOf(null as ExerciseGroup?) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // watch for selected exercises coming back from search exercise screen
                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<String>("selectedExercises")?.observe(lifecycleOwner) { result ->
                        if (editingGroup == null) {
                            // create a group from those exercises
                            createGroup(result.split("|"))
                        } else {
                            updateGroupExercises(editingGroup!!, result.split("|"))
                            editingGroup = null
                        }

                        // remove the handle so we don't duplicate the action
                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<String>("selectedExercises")
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (val state = groupState.value) {
        is DataState.Loading, is DataState.NotSent -> { CircularProgressIndicator(modifier = modifier) }
        is DataState.Error -> {
            state.e.localizedMessage?.let { Text(it, modifier = modifier) }
        }
        is DataState.Success -> {
            LazyColumn(modifier = modifier) {
                items(state.data) { group ->
                    ExerciseGroupSummaryCard(
                        group,
                        renameGroup = { newName ->
                            viewModel.renameGroup(group, newName)
                        },
                        editExercises = {
                            editingGroup = group
                            navController.navigate(
                                "${FitnessJournalScreen.SearchExercisesMultiSelectScreen.route}?" +
                                        "selectedExercises=${group.exercises.joinToString("|") { it.name }}"
                            )
                        },
                        removeGroup = { viewModel.removeGroup(group) },
                        onTap = if (selectable) ({
                            navController.previousBackStackEntry?.savedStateHandle?.set<Long>(
                                "selectedGroup",
                                group.id
                            )
                            navController.popBackStack()
                        }) else null
                    )
                }
            }
        }
    }

}