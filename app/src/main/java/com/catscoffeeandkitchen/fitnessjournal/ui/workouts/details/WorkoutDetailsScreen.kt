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
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.navigation.FitnessJournalScreen
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.ExerciseNavigableActions
import timber.log.Timber

@Composable
fun WorkoutDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: WorkoutDetailsViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    ) {
    val onAddExercise by rememberUpdatedState(viewModel::addExercise)
    val onSwapExercise by rememberUpdatedState(viewModel::swapExercise)
    val refreshWorkout by rememberUpdatedState(viewModel::getWorkout)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshWorkout()

                navController.currentBackStackEntry?.savedStateHandle
                    ?.getLiveData<String>("exerciseToAdd")?.observe(lifecycleOwner) { result ->
                        val swapping = navController.currentBackStackEntry
                            ?.savedStateHandle?.get<Int>("swappingExercise")
                        if (swapping != null) {
                            onSwapExercise(swapping, Exercise(result))
                        } else {
                            onAddExercise(result)
                        }

                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<String>("exerciseToAdd")
                        navController.currentBackStackEntry?.savedStateHandle
                            ?.remove<Int>("swappingExercise")
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
            is DataState.NotSent -> { }
            is DataState.Loading -> {
                if (viewModel.cachedWorkout != null) {
                    WorkoutDetails(
                        listState,
                        workout = viewModel.cachedWorkout!!,
                        sets = viewModel.cachedExercises,
                        unit = viewModel.weightUnit.value,
                        workoutActions = null,
                        exerciseUiActions = null,
                        exerciseNavigableActions = null,
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            is DataState.Success -> {
                WorkoutDetails(
                    listState,
                    workout = workoutState.data,
                    sets = if (workoutState.data.completedAt != null) viewModel.finishedExercises else viewModel.exercises,
                    unit = viewModel.weightUnit.value,
                    workoutActions = object : WorkoutActions {
                        override fun updateName(name: String) {
                            viewModel.updateWorkoutName(name)
                        }

                        override fun updateNote(note: String?) {
                            viewModel.updateWorkoutNote(note)
                        }

                        override fun finish() {
                            viewModel.finishWorkout()
                            navController.popBackStack()
                        }

                        override fun createPlanFromWorkout() {
                            viewModel.createPlanFromWorkout()
                            navController.navigate(FitnessJournalScreen.WorkoutPlansScreen.route)
                        }

                    },
                    exerciseUiActions = viewModel,
                    exerciseNavigableActions = object : ExerciseNavigableActions {
                        override fun addExercise() {
                            navController.navigate(FitnessJournalScreen.SearchExercisesScreen.route)
                        }

                        override fun addExerciseGroup() { }

                        override fun swapExercise(exercise: Exercise) {
                            navController.currentBackStackEntry?.savedStateHandle?.set("swappingExercise", exercise.positionInWorkout)
                            navController.navigate("${FitnessJournalScreen.SearchExercisesScreen.route}?" +
                                    "category=${exercise.category}&" +
                                    "muscle=${exercise.musclesWorked.firstOrNull().orEmpty()}")
                        }

                        override fun editGroup(group: ExerciseGroup) {
                            navController.navigate(
                                "${FitnessJournalScreen.SearchExercisesMultiSelectScreen.route}?" +
                                        "selectedExercises=${group.exercises.joinToString("|") { it.name }}")
                        }
                    },
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
