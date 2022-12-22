package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.usecases.exercise.GetExercisesUseCase
import com.catscoffeeandkitchen.domain.usecases.GetSetsAndExercisesUseCase
import com.catscoffeeandkitchen.domain.usecases.GetWorkoutDatesUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val getSetsUseCase: GetSetsAndExercisesUseCase,
    private val getWorkoutDatesUseCase: GetWorkoutDatesUseCase,
) : ViewModel() {
    private val _selectedExercise = MutableSharedFlow<String?>()
    private val _selectedExerciseRequest: Flow<String?>
    val selectedExerciseRequest: Flow<String?>
        get() = _selectedExerciseRequest

    private var _sets: MutableState<DataState<List<ExerciseSet>>> = mutableStateOf(DataState.NotSent())
    val sets: State<DataState<List<ExerciseSet>>> = _sets

    private var _exercises: MutableState<DataState<List<Exercise>>> = mutableStateOf(DataState.NotSent())
    val exercises: State<DataState<List<Exercise>>> = _exercises

    private var _workoutDates: MutableState<DataState<List<OffsetDateTime>>> = mutableStateOf(DataState.NotSent())
    val workoutDates: State<DataState<List<OffsetDateTime>>> = _workoutDates

    init {
        getExercises()
        getWorkoutDates()

        _selectedExerciseRequest = _selectedExercise.distinctUntilChanged()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )


        viewModelScope.launch {
            selectedExerciseRequest.flatMapLatest { request ->
                getSetsUseCase.run(request)
            }.collect { dataState ->
                _sets.value = dataState
            }
        }
    }

    private fun getExercises() = viewModelScope.launch {
        getExercisesUseCase.run().collect { state ->
            _exercises.value = state
        }
    }

    private fun getWorkoutDates(months: Int = 3) = viewModelScope.launch {
        getWorkoutDatesUseCase.run(months).collect { state ->
            _workoutDates.value = state
        }
    }

    fun selectExercise(name: String) = viewModelScope.launch {
        _selectedExercise.emit(name)
        _sets.value = DataState.NotSent()
    }

    fun getSetsForExercise(name: String) = viewModelScope.launch {
        getSetsUseCase.run(name).collect { state ->
            _sets.value = state
        }
    }


}
