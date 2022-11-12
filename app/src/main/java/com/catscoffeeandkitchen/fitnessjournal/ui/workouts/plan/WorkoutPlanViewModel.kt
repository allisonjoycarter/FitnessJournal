package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.usecases.workoutplan.*
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel()
class WorkoutPlanViewModel @Inject constructor(
    private val getWorkoutByAddedDateUseCase: GetWorkoutPlanByAddedDateUseCase,
    private val updateWorkoutPlanUseCase: UpdateWorkoutPlanUseCase,
    private val addSetToWorkoutPlan: AddSetToWorkoutPlanUseCase,
    private val removeSetFromWorkoutPlan: RemoveSetFromWorkoutPlanUseCase,
    private val updateExpectedSet: UpdateExpectedSetUseCase,
    private val createWorkoutPlanUseCase: CreateWorkoutPlanUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private var _workoutPlan: MutableState<DataState<WorkoutPlan>> = mutableStateOf(DataState.NotSent())
    val workoutPlan: State<DataState<WorkoutPlan>> = _workoutPlan

    init {
        val workoutDate = savedStateHandle.get<Long?>("workoutId")
        if (workoutDate != null) {
            if (workoutDate == 0L) {
                val now = OffsetDateTime.now()
                viewModelScope.launch {
                    createWorkoutPlanUseCase.run(WorkoutPlan(addedAt = now)
                    ).collect { wo ->
                        Timber.d(" workout = $wo")
                        _workoutPlan.value = wo
                    }
                }
                savedStateHandle["workoutId"] = now.toInstant().toEpochMilli()
            } else {
                viewModelScope.launch {
                    getWorkoutByAddedDateUseCase.run(
                        OffsetDateTime.ofInstant(Instant.ofEpochMilli(workoutDate), ZoneOffset.UTC)
                    ).collect { wo ->
                        Timber.d(" workout = $wo")
                        _workoutPlan.value = wo
                    }
                }
            }
        }
    }

    fun updateExercisePlan(setNumber: Int, field: ExercisePlanField, value: Int) = viewModelScope.launch {
        Timber.d("*** updating workout plan")
        (workoutPlan.value as? DataState.Success)?.data?.let { workout ->
            val setFromWorkout = workout.exercises.find { it.setNumberInWorkout == setNumber }
            Timber.d("*** updating set number ${setFromWorkout?.setNumberInWorkout}")
            if (setFromWorkout != null) {
                updateExpectedSet.run(
                    workout,
                    getUpdatedExpectedSetField(setFromWorkout, field, value)
                ).collect { result ->
                    if (result is DataState.Success) {
                        getWorkoutByAddedDateUseCase.run(workout.addedAt).collect { wo ->
                            Timber.d("updated plan = $wo")
                            _workoutPlan.value = wo
                        }
                    }
                }
            }
        }
    }

    fun addExercise(name: String) = viewModelScope.launch {
        Timber.d("*** adding exercise $name to plan")
        val workout = (workoutPlan.value as? DataState.Success)?.data
        if (workout != null) {
            addSetToWorkoutPlan.run(
                workout,
                ExpectedSet(
                    exercise = Exercise(name, musclesWorked = emptyList())
                )
            ).collect { result ->
                if (result is DataState.Success) {
                    getWorkoutByAddedDateUseCase.run(workout.addedAt).collect { wo ->
                        Timber.d("added to workout = $wo")
                        _workoutPlan.value = wo
                    }
                }
            }
        }
    }

    fun removeSet(expectedSet: ExpectedSet) = viewModelScope.launch {
        (workoutPlan.value as? DataState.Success)?.data?.let { workout ->
            removeSetFromWorkoutPlan.run(
                workout,
                expectedSet
            ).collect { result ->
                if (result is DataState.Success) {
                    getWorkoutByAddedDateUseCase.run(workout.addedAt).collect { wo ->
                        Timber.d("removed from workout = $wo")
                        _workoutPlan.value = wo
                    }
                }
            }
        }
    }

    fun updateWorkoutName(name: String) = viewModelScope.launch {
        (workoutPlan.value as? DataState.Success)?.data?.let { currentWorkout ->
            _workoutPlan.value = DataState.Loading()
            val updatedWorkout = currentWorkout.copy(name = name)
            updateWorkoutPlanUseCase.run(updatedWorkout).collect { updated ->
                if (updated is DataState.Success) {
                    _workoutPlan.value = DataState.Success(updatedWorkout)
                } else if (updated is DataState.Error) {
                    _workoutPlan.value = DataState.Error(updated.e)
                }
            }
        }
    }

    fun updateWorkoutNotes(note: String) = viewModelScope.launch {
        (workoutPlan.value as? DataState.Success)?.data?.let { currentWorkout ->
            _workoutPlan.value = DataState.Loading()
            val updatedWorkout = currentWorkout.copy(note = note)
            updateWorkoutPlanUseCase.run(updatedWorkout).collect { updated ->
                if (updated is DataState.Success) {
                    _workoutPlan.value = DataState.Success(updatedWorkout)
                } else if (updated is DataState.Error) {
                    _workoutPlan.value = DataState.Error(updated.e)
                }
            }
        }
    }

    private fun getUpdatedExpectedSetField(set: ExpectedSet, field: ExercisePlanField, value: Int): ExpectedSet {
        return when (field) {
            ExercisePlanField.Reps -> set.copy(reps = value)
            ExercisePlanField.MaxReps -> set.copy(maxReps = value)
            ExercisePlanField.MinReps -> set.copy(minReps = value)
            ExercisePlanField.Sets -> set.copy(sets = value)
            ExercisePlanField.WeightInPounds -> set
            ExercisePlanField.WeightInKilograms -> set
            ExercisePlanField.RepsInReserve -> set.copy(rir = value)
            ExercisePlanField.PerceivedExertion -> set.copy(perceivedExertion = value)
        }
    }
}