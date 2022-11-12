package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.list

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.usecases.CreateWorkoutUseCase
import com.catscoffeeandkitchen.domain.usecases.GetPagedWorkoutsUseCase
import com.catscoffeeandkitchen.domain.usecases.GetWorkoutsUseCase
import com.catscoffeeandkitchen.domain.usecases.RemoveWorkoutUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
//    private val getWorkoutsUseCase: GetWorkoutsUseCase,
    private val getPagedWorkoutsUseCase: GetPagedWorkoutsUseCase,
    private val removeWorkoutUseCase: RemoveWorkoutUseCase,
): ViewModel() {
//    private var _workouts: MutableState<DataState<List<Workout>>> = mutableStateOf(DataState.Loading())
//    val workouts: State<DataState<List<Workout>>> = _workouts

    private var _pagedWorkouts: Flow<PagingData<Workout>> = getPagedWorkoutsUseCase.run()
    val pagedWorkout: Flow<PagingData<Workout>>
        get() = _pagedWorkouts
//
//    init {
//        getWorkouts()
//
//    }
//
//    fun getWorkouts() = viewModelScope.launch {
//        getWorkoutsUseCase.run()
//            .distinctUntilChanged()
//            .collect { wos ->
//                Timber.d("*** Workouts $wos")
//                _workouts.value = wos
//            }
//    }

    fun deleteWorkout(workout: Workout) = viewModelScope.launch {
        removeWorkoutUseCase.run(workout).collect { state ->
            if (state is DataState.Success) {
                _pagedWorkouts = getPagedWorkoutsUseCase.run()
            }
        }
    }
}