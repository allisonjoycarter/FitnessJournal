package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.usecases.GetWorkoutPlansUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WorkoutPlansViewModel @Inject constructor(
    private val getWorkoutPlansUseCase: GetWorkoutPlansUseCase
): ViewModel() {
    private var _workouts: MutableState<DataState<List<WorkoutPlan>>> = mutableStateOf(DataState.Loading())
    val workouts: State<DataState<List<WorkoutPlan>>> = _workouts

    init {
        getWorkouts()
    }

    fun getWorkouts() = viewModelScope.launch {
        _workouts.value = DataState.Loading()
        getWorkoutPlansUseCase.run()
            .collect { wos ->
                _workouts.value = wos
            }
    }
}