package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.exercisegroups

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.usecases.exercisegroup.*
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExerciseGroupsViewModel @Inject constructor(
    private val getExerciseGroupsUseCase: GetExerciseGroupsUseCase,
    private val createExerciseGroupUseCase: CreateExerciseGroupUseCase,
    private val updateExerciseGroupUseCase: UpdateExerciseGroupUseCase,
    private val updateExercisesInGroupUseCase: UpdateExercisesInGroupUseCase,
    private val removeGroupUseCase: RemoveExerciseGroupUseCase,
    savedInstanceState: SavedStateHandle
): ViewModel() {

    private var _exerciseGroups: MutableStateFlow<DataState<List<ExerciseGroup>>> = MutableStateFlow(DataState.NotSent())
    val exerciseGroups: StateFlow<DataState<List<ExerciseGroup>>> = _exerciseGroups

    init {
        viewModelScope.launch {
            getExerciseGroupsUseCase.run().collect { groups ->
                _exerciseGroups.emit(groups)
            }
        }
    }

    fun createGroup(names: List<String>) = viewModelScope.launch {
        createExerciseGroupUseCase.run(names, "group").collect { state ->
            if (state is DataState.Success) {
                getExerciseGroupsUseCase.run().collect { groups ->
                    _exerciseGroups.emit(groups)
                }
            } else {
                Timber.d((state as? DataState.Error)?.e ?: Exception("createExercisesGroupUseCase: $state"))
            }
        }
    }

    fun renameGroup(group: ExerciseGroup, updatedName: String) = viewModelScope.launch {
        updateExerciseGroupUseCase.run(group.copy(name = updatedName)).collect { state ->
            if (state is DataState.Success) {
                getExerciseGroupsUseCase.run().collect { groups ->
                    _exerciseGroups.emit(groups)
                }
            }
        }
    }

    fun updateGroupExercises(group: ExerciseGroup, exercises: List<String>) = viewModelScope.launch {
        updateExercisesInGroupUseCase.run(group, exercises).collect { state ->
            if (state is DataState.Success) {
                getExerciseGroupsUseCase.run().collect { groups ->
                    _exerciseGroups.emit(groups)
                }
            }
        }
    }

    fun removeGroup(group: ExerciseGroup) = viewModelScope.launch {
        removeGroupUseCase.run(group).collect { state ->
            if (state is DataState.Success) {
                getExerciseGroupsUseCase.run().collect { groups ->
                    _exerciseGroups.emit(groups)
                }
            }
        }
    }
}