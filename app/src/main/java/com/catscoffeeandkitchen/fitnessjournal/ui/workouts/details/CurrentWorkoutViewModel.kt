package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.usecases.*
import com.catscoffeeandkitchen.domain.usecases.exercise.RemoveExerciseFromWorkoutUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.AddExerciseSetUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.AddMultipleExerciseSetsUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.ReplaceExerciseForSetsUseCase
import com.catscoffeeandkitchen.domain.usecases.workoutplan.CreatePlanFromWorkoutUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.util.SharedPrefsConstants.WeightUnitKey
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit.Pounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CurrentWorkoutViewModel @Inject constructor(
    private val createWorkoutUseCase: CreateWorkoutUseCase,
    private val createPlanFromWorkoutUseCase: CreatePlanFromWorkoutUseCase,
    private val updateWorkoutUseCase: UpdateWorkoutUseCase,
    private val addSetUseCase: AddExerciseSetUseCase,
    private val addMultipleSetsUseCase: AddMultipleExerciseSetsUseCase,
    private val removeExerciseFromWorkoutUseCase: RemoveExerciseFromWorkoutUseCase,
    private val replaceExerciseForSetsUseCase: ReplaceExerciseForSetsUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val getWorkoutByAddedDateUseCase: GetWorkoutByAddedDateUseCase,
    private val removeSetUseCase: RemoveSetUseCase,
    private val sharedPreferences: SharedPreferences,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    var cachedWorkout: Workout? = null

    private var _workout: MutableState<DataState<Workout>> = mutableStateOf(DataState.NotSent())
    val workout: State<DataState<Workout>> = _workout

    private var _weightUnit: MutableState<WeightUnit> = mutableStateOf(Pounds)
    val weightUnit: State<WeightUnit> = _weightUnit

    private var _createdPlanAddedAt: MutableState<DataState<OffsetDateTime>> = mutableStateOf(DataState.NotSent())
    val createdPlanAddedAt: State<DataState<OffsetDateTime>> = _createdPlanAddedAt

    private val workoutInstance: Workout?
        get() =
            (_workout.value as? DataState.Success)?.data

    init {
        val workoutDate = savedStateHandle.get<Long>("workoutId")
        val plan = savedStateHandle.get<String?>("plan")
        if (workoutDate != null) {
            if (workoutDate == 0L && _workout.value is DataState.NotSent) {
                val now = OffsetDateTime.now()
                viewModelScope.launch {
                    createWorkoutUseCase.run(
                        Workout(
                            addedAt = now,
                        ),
                        planAddedAt = if (plan == null) null else
                            OffsetDateTime.ofInstant(Instant.ofEpochMilli(plan.toLong()), ZoneOffset.UTC)
                    ).collect { _workout.value = it }
                }
                savedStateHandle["workoutId"] = now.toInstant().toEpochMilli()
            } else {
                viewModelScope.launch {
                    getWorkoutByAddedDateUseCase.run(
                        OffsetDateTime.ofInstant(Instant.ofEpochMilli(workoutDate), ZoneOffset.UTC)
                    ).collect { wo ->
                        _workout.value = wo
                        cachedWorkout = (wo as? DataState.Success)?.data
                    }
                }
            }
        }

        val unit = sharedPreferences.getInt(WeightUnitKey, 0)
        _weightUnit.value = WeightUnit.values().firstOrNull { it.ordinal == unit } ?: Pounds
    }

    fun getWorkout() = viewModelScope.launch {
        workoutInstance?.let { currentWorkout ->
            viewModelScope.launch {
                getWorkoutByAddedDateUseCase.run(currentWorkout.addedAt).collect { wo ->
                    _workout.value = wo
                    cachedWorkout = (wo as? DataState.Success)?.data
                }
            }
        }
    }

    fun addExerciseSet(
        name: String,
    ) = viewModelScope.launch {
        val exercise = Exercise(
            name,
            emptyList(),
        )

        val instance = workoutInstance
        if (instance != null) {
            val nextSetNumber = (instance.sets
                .maxOfOrNull { it.setNumberInWorkout } ?: 0) + 1
            Timber.d("*** inserting $name at $nextSetNumber, current sets are " +
                    "${instance.sets.map { it.exercise.name to it.setNumberInWorkout }}")
            addSetUseCase.run(
                exercise = exercise,
                workout = instance,
                set = ExerciseSet(
                    0L,
                    exercise = exercise,
                    reps = 0,
                    setNumberInWorkout = nextSetNumber
                )
            ).collect { state ->
                if (state is DataState.Success) {
                    getWorkout()
                }
            }
        }
    }

    fun swapExercise(
        setNumberToSwap: Int,
        name: String,
    ) = viewModelScope.launch {
        val exercise = Exercise(
            name,
            emptyList(),
        )

        val instance = workoutInstance
        if (instance != null) {
            val setToSwap = instance.sets.find { it.setNumberInWorkout == setNumberToSwap }
            if (setToSwap != null) {
                // TODO: only remove group of sets, not all instances of that exercise
                val relatedSets = instance.sets
                    .filter { it.exercise.name == setToSwap.exercise.name }

                replaceExerciseForSetsUseCase.run(relatedSets.map { it.id }, exercise).collect { state ->
                    if (state is DataState.Success) {
                        val updatedWorkout = instance.copy(
                            sets = instance.sets.map { set ->
                                if (relatedSets.contains(set)) set.copy(exercise = exercise)
                                else set
                            }
                        )
                        _workout.value = DataState.Success(updatedWorkout)
                    }
                }
            }
        }
    }

    fun updateSet(
        exerciseSet: ExerciseSet,
    ) = viewModelScope.launch {
        updateSetUseCase.run(
            set = exerciseSet,
        ).collect { successful ->
                if (successful is DataState.Success) {
                    workoutInstance?.let { instance ->
                        val updated = instance.copy(
                            sets = instance.sets.map { set ->
                                if (set.id == exerciseSet.id) exerciseSet
                                else set
                            }
                        )
                        _workout.value = DataState.Success(updated)
                    }
                }
            }
    }

    fun removeSet(
        setId: Long
    ) = viewModelScope.launch {
        removeSetUseCase.run(setId).collect { dataState ->
            Timber.d("removing set $dataState")
            if (dataState is DataState.Error) {
                Timber.e(dataState.e)
            }
            if (dataState is DataState.Success && workout.value is DataState.Success) {
                getWorkout()
            }
        }
    }

    fun removeExercise(exercise: Exercise) = viewModelScope.launch {
        (_workout.value as? DataState.Success)?.data?.let { currentWorkout ->
            Timber.d("removing exercise ${exercise.name} from current workout")
            removeExerciseFromWorkoutUseCase.run(exercise, currentWorkout).collect { dataState ->
                Timber.d("removing exercise $dataState")
                if (dataState is DataState.Error) {
                    Timber.e(dataState.e)
                }
                if (dataState is DataState.Success) {
                    getWorkout()
                }
            }
        }
    }

    fun updateWorkoutName(name: String) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { currentWorkout ->
            _workout.value = DataState.Loading()
            val updatedWorkout = currentWorkout.copy(name = name)
            updateWorkoutUseCase.run(updatedWorkout).collect { updated ->
                if (updated is DataState.Success) {
                    cachedWorkout = updatedWorkout
                } else if (updated is DataState.Error) {
                    _workout.value = DataState.Error(updated.e)
                }
            }
        }
    }

    fun finishWorkout() = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { currentWorkout ->
            _workout.value = DataState.Loading()
            val updatedWorkout = currentWorkout.copy(completedAt = OffsetDateTime.now())
            updateWorkoutUseCase.run(updatedWorkout).collect { updated ->
                if (updated is DataState.Success) {
                    cachedWorkout = updatedWorkout
                } else if (updated is DataState.Error) {
                    _workout.value = DataState.Error(updated.e)
                }
            }
        }
    }

    fun updateWorkoutNote(note: String?) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { currentWorkout ->
            _workout.value = DataState.Loading()
            val updatedWorkout = currentWorkout.copy(note = note)
            updateWorkoutUseCase.run(updatedWorkout).collect { updated ->
                if (updated is DataState.Success) {
                    _workout.value = DataState.Success(updatedWorkout)
                    cachedWorkout = updatedWorkout
                } else if (updated is DataState.Error) {
                    _workout.value = DataState.Error(updated.e)
                }
            }
        }
    }

    fun addWarmupSets(exercise: Exercise) = viewModelScope.launch {
        val workoutInstance = (workout.value as? DataState.Success<Workout>)?.data
        if (workoutInstance != null) {
            val exerciseSets = workoutInstance.sets.filter { it.exercise.name == exercise.name }
            val highWeight = if (weightUnit.value == WeightUnit.Pounds)
                exerciseSets.maxOf { it.weightInPounds } else
                exerciseSets.maxOf { it.weightInKilograms }
            var nextSetNumber = exerciseSets.minOf { it.setNumberInWorkout }

            val setsToAdd = arrayListOf(
                ExerciseSet(
                    0L,
                    exercise = exercise,
                    reps = 12,
                    setNumberInWorkout = nextSetNumber,
                    weightInPounds = if (highWeight > 100f) 45f else (highWeight * .25f),
                    weightInKilograms = if (highWeight > 100f) 45f else (highWeight * .25f),
                    type = ExerciseSetType.WarmUp
                )
            )
            val increments = mapOf(
                .575 to 8,
                .725 to 5,
                .825 to 3,
                .925 to 1)
            increments.forEach { warmupSet ->
                nextSetNumber++
                setsToAdd.add(
                    ExerciseSet(
                        0L,
                        exercise = exercise,
                        reps = warmupSet.value,
                        setNumberInWorkout = nextSetNumber,
                        weightInPounds = round(warmupSet.key * highWeight, 5).toFloat(),
                        weightInKilograms = round(warmupSet.key * highWeight, 5).toFloat(),
                        type = ExerciseSetType.WarmUp
                    )
                )
            }

            addMultipleSetsUseCase.run(
                exercise = exercise,
                workout = workoutInstance,
                sets = setsToAdd
            ).collect { state ->
                if (state is DataState.Success) {
                    getWorkout()
                }
            }
        }
    }

    fun createPlanFromWorkout() = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { wkot ->
            createPlanFromWorkoutUseCase.run(wkot).collect { dataState ->
                _createdPlanAddedAt.value = dataState
            }
        }
    }

    private fun round(value: Double, nearest: Int): Int {
        return (value / nearest).roundToInt() * nearest
    }

}