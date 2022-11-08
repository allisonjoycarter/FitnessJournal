package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExerciseModifierViewModel @Inject constructor(): ViewModel() {
    private var _setIndex: MutableState<Int> = mutableStateOf(0)
    val setIndex: State<Int> = _setIndex

//    private var _exerciseSet: MutableState<ExerciseSet?> = mutableStateOf(null)
//    val exerciseSet: State<ExerciseSet?> = _exerciseSet

    private var _exercise: MutableState<Exercise?> = mutableStateOf(null)
    val exercise: State<Exercise?> = _exercise

    fun setExercise(exercise: Exercise?) {
        _exercise.value = exercise
    }

    fun setSetIndex(index: Int) {
        _setIndex.value = index
    }
}