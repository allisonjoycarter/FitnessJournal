@file:OptIn(ExperimentalMaterialApi::class)

package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
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
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.ExerciseSetField

@Composable
fun WorkoutDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CurrentWorkoutViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
    val onAddExercise by rememberUpdatedState(viewModel::addExerciseSet)
    val onSwapExercise by rememberUpdatedState(viewModel::swapExercise)
    val refreshWorkout by rememberUpdatedState(viewModel::getWorkout)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshWorkout()
                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<String>("exerciseToAdd")?.observe(lifecycleOwner) { result ->
                        val setToSwap = navController.currentBackStackEntry
                            ?.savedStateHandle?.get<Int>("swappingExerciseSet")
                        if (setToSwap != null) {
                            onSwapExercise(setToSwap, result)
                        } else {
                            onAddExercise(result)
                        }

                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<String>("exerciseToAdd")
                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<Int>("swappingExerciseSet")
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val listState = rememberLazyListState()

    Column(
        modifier = modifier
    ) {
        when (val workoutState = viewModel.workout.value) {
            is DataState.NotSent -> {}
            is DataState.Loading -> {
                if (viewModel.cachedWorkout != null) {
                    WorkoutDetails(
                        listState,
                        workout = viewModel.cachedWorkout!!,
                        unit = viewModel.weightUnit.value
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            is DataState.Success -> {
                WorkoutDetails(
                    listState,
                    workout = workoutState.data,
                    unit = viewModel.weightUnit.value,
                    updateWorkoutName = { title -> viewModel.updateWorkoutName(title) },
                    updateWorkoutNote = { note -> viewModel.updateWorkoutNote(note) },
                    addExercise = {
                        navController.navigate(FitnessJournalScreen.SearchExercisesScreen.route)
                    },
                    addSet = { viewModel.addExerciseSet(it.name) },
                    addWarmupSets = { viewModel.addWarmupSets(it) },
                    removeExercise = { viewModel.removeExercise(it) },
                    removeSet = { viewModel.removeSet(it.id) },
                    updateExercise = { set: ExerciseSet, field: ExerciseSetField ->
                        viewModel.updateSet(field.copySetWithNewValue(set))
                    },
                    createPlanFromWorkout = {
                        viewModel.createPlanFromWorkout()
                        navController.navigate(FitnessJournalScreen.WorkoutPlansScreen.route)
                    },
                    swapExercise = { swapping, exercise ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("swappingExerciseSet", swapping)
                        navController.navigate("${FitnessJournalScreen.SearchExercisesScreen.route}?" +
                                "category=${exercise.category}&" +
                                "muscle=${exercise.musclesWorked.firstOrNull().orEmpty()}")
                    },
                    finish = {
                        viewModel.finishWorkout()
                        navController.popBackStack()
                    }
                )
            }
            is DataState.Error -> {
                Text("Error = ${workoutState.e.localizedMessage}")
            }
            else -> {
                Text(text = "State = $workoutState")
            }
        }
    }
}
