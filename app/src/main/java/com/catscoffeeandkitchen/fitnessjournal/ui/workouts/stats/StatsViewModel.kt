package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.stats

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.usecases.GetExercisesUseCase
import com.catscoffeeandkitchen.domain.usecases.GetSetsAndExercisesUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getExercisesUseCase: GetExercisesUseCase,
    private val getSetsUseCase: GetSetsAndExercisesUseCase
) : ViewModel() {
    private val _selectedExercise = MutableSharedFlow<String?>()
    private val _selectedExerciseRequest: Flow<String?>
    val selectedExerciseRequest: Flow<String?>
        get() = _selectedExerciseRequest

    private var _sets: MutableState<DataState<List<ExerciseSet>>> = mutableStateOf(DataState.NotSent())
    val sets: State<DataState<List<ExerciseSet>>> = _sets

    private var _exercises: MutableState<DataState<List<Exercise>>> = mutableStateOf(DataState.NotSent())
    val exercises: State<DataState<List<Exercise>>> = _exercises

    init {
        getExercises()

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

    fun getExercises() = viewModelScope.launch {
        getExercisesUseCase.run().collect { state ->
            _exercises.value = state
        }
    }

    fun selectExercise(name: String) = viewModelScope.launch {
        _selectedExercise.emit(name)
    }

    fun getSetsForExercise(name: String) = viewModelScope.launch {
        getSetsUseCase.run(name).collect { state ->
            _sets.value = state
        }
    }


}
