package com.catscoffeeandkitchen.fitnessjournal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.ExerciseProgressStats
import com.catscoffeeandkitchen.domain.models.WorkoutEntry
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.models.WorkoutWeekStats
import com.catscoffeeandkitchen.domain.usecases.home.GetLastExercisesCompletedUseCase
import com.catscoffeeandkitchen.domain.usecases.home.GetMostImprovedExerciseUseCase
import com.catscoffeeandkitchen.domain.usecases.home.GetNextWorkoutPlanUseCase
import com.catscoffeeandkitchen.domain.usecases.home.GetWorkoutsPerWeekUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNextWorkoutPlanUseCase: GetNextWorkoutPlanUseCase,
    private val getLastExercisesCompletedUseCase: GetLastExercisesCompletedUseCase,
    private val getMostImprovedExerciseUseCase: GetMostImprovedExerciseUseCase,
    private val getWorkoutsPerWeekUseCase: GetWorkoutsPerWeekUseCase
): ViewModel() {
    private var _nextWorkoutPlan: MutableStateFlow<DataState<WorkoutPlan?>> = MutableStateFlow(DataState.NotSent())
    val nextWorkoutPlan: StateFlow<DataState<WorkoutPlan?>> = _nextWorkoutPlan

    private var _lastExercisesCompleted: MutableStateFlow<DataState<List<WorkoutEntry>>> = MutableStateFlow(DataState.NotSent())
    val lastExercisesCompleted: StateFlow<DataState<List<WorkoutEntry>>> = _lastExercisesCompleted

    private var _mostImprovedExercise: MutableStateFlow<DataState<ExerciseProgressStats?>> = MutableStateFlow(DataState.NotSent())
    val mostImprovedExercise: StateFlow<DataState<ExerciseProgressStats?>> = _mostImprovedExercise

    private var _weekStats: MutableStateFlow<DataState<WorkoutWeekStats>> = MutableStateFlow(DataState.NotSent())
    val weekStats: StateFlow<DataState<WorkoutWeekStats>> = _weekStats

    init {
        viewModelScope.launch {
            getNextWorkoutPlanUseCase.run().collect { _nextWorkoutPlan.value = it }
        }

        viewModelScope.launch {
            getLastExercisesCompletedUseCase.run().collect { _lastExercisesCompleted.value = it }
        }

        viewModelScope.launch {
            getMostImprovedExerciseUseCase.run(12).collect {
                _mostImprovedExercise.value = it
            }
        }

        viewModelScope.launch {
            getWorkoutsPerWeekUseCase.run(4 * 6).collect { _weekStats.value = it }
        }
    }
}