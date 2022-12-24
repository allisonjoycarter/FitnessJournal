package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseGroup
import com.catscoffeeandkitchen.domain.models.ExpectedSet
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.domain.usecases.exercisegroup.CreateExerciseGroupUseCase
import com.catscoffeeandkitchen.domain.usecases.workoutplan.*
import com.catscoffeeandkitchen.domain.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel()
class WorkoutPlanViewModel @Inject constructor(
    private val getPlanUseCase: GetWorkoutPlanUseCase,
    private val updateWorkoutPlanUseCase: UpdateWorkoutPlanUseCase,
    private val addSetToWorkoutPlan: AddSetToWorkoutPlanUseCase,
    private val createExerciseGroupUseCase: CreateExerciseGroupUseCase,
    private val removeSetFromWorkoutPlan: RemoveSetFromWorkoutPlanUseCase,
    private val updateExpectedSet: UpdateExpectedSetUseCase,
    private val updateExercisePositionUseCase: UpdateExpectedSetPositionUseCase,
    private val createWorkoutPlanUseCase: CreateWorkoutPlanUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private var _workoutPlan: MutableState<DataState<WorkoutPlan>> = mutableStateOf(DataState.NotSent())
    val workoutPlan: State<DataState<WorkoutPlan>> = _workoutPlan

    private var _showExerciseGroupNameDialog = MutableStateFlow(false)
    val showExerciseGroupNameDialog = _showExerciseGroupNameDialog
    private var _exercisesToGroup = MutableStateFlow(emptyList<String>())
    var exercisesToGroup = _exercisesToGroup

    val planInstance
        get() = (workoutPlan.value as? DataState.Success)?.data

    init {
        val planId = savedStateHandle.get<Long?>("workoutId")
        if (planId != null) {
            if (planId == 0L) {
                viewModelScope.launch {
                    createWorkoutPlanUseCase.run(WorkoutPlan(id = 0L)
                    ).collect { wo ->
                        _workoutPlan.value = wo

                        if (wo is DataState.Success) {
                            savedStateHandle["workoutId"] = wo.data.id
                        }
                    }
                }
            } else {
                viewModelScope.launch {
                    getPlanUseCase.run(planId).collect { wo ->
                        Timber.d("*** ${(wo as? DataState.Success)?.data?.entries}")
                        _workoutPlan.value = wo
                    }
                }
            }
        }
    }

    fun updateExercisePlan(setNumber: Int, field: ExercisePlanField, value: Int) = viewModelScope.launch {
        (workoutPlan.value as? DataState.Success)?.data?.let { workout ->
            val setFromWorkout = workout.entries.find { it.positionInWorkout == setNumber }
            if (setFromWorkout != null) {
                updateExpectedSet.run(
                    workout,
                    getUpdatedExpectedSetField(setFromWorkout, field, value)
                ).collect { result ->
                    if (result is DataState.Success) {
                        getPlanUseCase.run(workout.id).collect { wo ->
                            Timber.d("updated plan = $wo")
                            _workoutPlan.value = wo
                        }
                    }
                }
            }
        }
    }

    fun updateExercisePosition(originalSetNumber: Int, newSetNumber: Int) = viewModelScope.launch {
        planInstance?.let { workout ->
            val setFromWorkout = workout.entries.find { it.positionInWorkout == originalSetNumber }
            setFromWorkout?.let { set ->
                updateExercisePositionUseCase.run(
                    workout,
                    set,
                    newSetNumber,
                ).collect { result ->
                    if (result is DataState.Success) {
                        getPlanUseCase.run(workout.id).collect { wo ->
                            Timber.d("updated plan = $wo")
                            _workoutPlan.value = wo
                        }
                    }
                }
            }
        }
    }

    fun addExercise(name: String) = viewModelScope.launch {
        val workout = (workoutPlan.value as? DataState.Success)?.data
        if (workout != null) {
            addSetToWorkoutPlan.run(
                workout,
                ExpectedSet(
                    exercise = Exercise(name, musclesWorked = emptyList()),
                    sets = 3,
                    reps = 10,
                    minReps = 8,
                    maxReps = 12,
                    perceivedExertion = 8,
                    rir = 2
                )
            ).collect { result ->
                if (result is DataState.Success) {
                    getPlanUseCase.run(workout.id).collect { wo ->
                        Timber.d("added to workout = $wo")
                        _workoutPlan.value = wo
                    }
                }
            }
        }
    }

    fun addExerciseGroup(names: List<String>, groupName: String = "") = viewModelScope.launch {
        (workoutPlan.value as? DataState.Success)?.data?.let { workout ->
            createExerciseGroupUseCase.run(names, groupName)
                .collect { state ->
                    Timber.d("*** create group state = $state")
                    if (state is DataState.Success) {
                        addSetToWorkoutPlan.run(
                            workout,
                            ExpectedSet(
                                exerciseGroup = state.data
                            )
                        ).collectLatest { addedSetState ->
                            if (addedSetState is DataState.Success) {
                                getPlanUseCase.run(workout.id).collect { wo ->
                                    _workoutPlan.value = wo
                                }
                            }
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
                    getPlanUseCase.run(workout.id).collect { wo ->
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

    fun updateWeekdays(weekdays: List<DayOfWeek>) = viewModelScope.launch {
        planInstance?.let { currentWorkout ->
            val updatedWorkout = currentWorkout.copy(daysOfWeek = weekdays)
            updateWorkoutPlanUseCase.run(updatedWorkout).collect { updated ->
                if (updated is DataState.Success) {
                    _workoutPlan.value = DataState.Success(updatedWorkout)
                } else if (updated is DataState.Error) {
                    _workoutPlan.value = DataState.Error(updated.e)
                }
            }
        }
    }

    fun selectGroup(id: Long) = viewModelScope.launch {
        (workoutPlan.value as? DataState.Success)?.data?.let { currentWorkout ->
            addSetToWorkoutPlan.run(
                currentWorkout,
                ExpectedSet(
                    exerciseGroup = ExerciseGroup(id = id, name = "", emptyList())
                )
            ).collectLatest { addedSetState ->
                if (addedSetState is DataState.Success) {
                    getPlanUseCase.run(currentWorkout.id).collect { wo ->
                        _workoutPlan.value = wo
                    }
                }
            }
        }
    }

    fun showGroupNameDialog(names: List<String>) {
        _showExerciseGroupNameDialog.value = true
        _exercisesToGroup.value = names
    }

    fun hideGroupNameDialog() {
        _showExerciseGroupNameDialog.value = false
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
            ExercisePlanField.SetNumber -> set
        }
    }
}