@file:OptIn(ExperimentalMaterialApi::class)

package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.ExerciseSetField
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.InputModalContent
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun WorkoutDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CurrentWorkoutViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
    val onStartOrResume by rememberUpdatedState(viewModel::addExerciseSet)
    val refreshWorkout by rememberUpdatedState(viewModel::getWorkout)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshWorkout()
                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<String>("exerciseToAdd")?.observe(lifecycleOwner) { result ->
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

    EditableWorkoutDetails(
        modifier = modifier,
        workout = viewModel.workout,
        cachedWorkout = viewModel.cachedWorkout,
        updateWorkoutName = { title -> viewModel.updateWorkoutName(title) },
        updateWorkoutNote = { note -> viewModel.updateWorkoutNote(note) },
        addExercise = {
            navController.navigate(FitnessJournalScreen.SearchExercisesScreen.route)
        },
        addSet = { viewModel.addExerciseSet(it.name) },
        addWarmupSets = { viewModel.addWarmupSets(it) },
        removeExercise = { viewModel.removeExercise(it) },
        removeSet = { viewModel.removeSet(it.id) },
        updateExercise = { set: ExerciseSet, field: ExerciseSetField, value: Int ->
            Timber.d("*** updating exercise $field = $value")
            viewModel.updateSet(field.copySetWithNewValue(set, value))
        },
        finish = {
            viewModel.finishWorkout()
            navController.popBackStack()
        }
    )
}
