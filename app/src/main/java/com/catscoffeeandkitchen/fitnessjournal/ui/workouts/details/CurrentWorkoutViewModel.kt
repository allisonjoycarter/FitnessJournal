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
import com.catscoffeeandkitchen.domain.util.DataState
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
    private val updateWorkoutUseCase: UpdateWorkoutUseCase,
    private val addSetUseCase: AddExerciseSetUseCase,
    private val addMultipleSetsUseCase: AddMultipleExerciseSetsUseCase,
    private val removeExerciseFromWorkoutUseCase: RemoveExerciseFromWorkoutUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val getWorkoutByAddedDateUseCase: GetWorkoutByAddedDateUseCase,
    private val removeSetUseCase: RemoveSetUseCase,
    private val sharedPreferences: SharedPreferences,
    savedStateHandle: SavedStateHandle,
): ViewModel() {
    var cachedWorkout: Workout? = null

    private var _workout: MutableState<DataState<Workout>> = mutableStateOf(DataState.NotSent())
    val workout: State<DataState<Workout>> = _workout

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
                        Timber.d(" workout = $wo")
                        _workout.value = wo
                        cachedWorkout = (wo as? DataState.Success)?.data
                    }
                }
            }
        }
    }

    fun getWorkout() = viewModelScope.launch {
        (_workout.value as? DataState.Success)?.data?.let { currentWorkout ->
            viewModelScope.launch {
                getWorkoutByAddedDateUseCase.run(currentWorkout.addedAt).collect { wo ->
                    Timber.d(" workout = $wo")
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

        val workoutInstance = (workout.value as? DataState.Success<Workout>)?.data
        if (workoutInstance != null) {
            val nextSetNumber = (workoutInstance.sets
                .maxOfOrNull { it.setNumberInWorkout } ?: 0) + 1
            Timber.d("*** inserting $name at $nextSetNumber, current sets are " +
                    "${workoutInstance.sets.map { it.exercise.name to it.setNumberInWorkout }}")
            addSetUseCase.run(
                exercise = exercise,
                workout = workoutInstance,
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

    fun updateSet(
        exerciseSet: ExerciseSet,
    ) = viewModelScope.launch {
        updateSetUseCase.run(
            set = exerciseSet,
        ).collect { successful ->
                if (successful is DataState.Success) {
                    getWorkout()
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
                    _workout.value = DataState.Success(updatedWorkout)
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
                    _workout.value = DataState.Success(updatedWorkout)
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
        val exercise = Exercise(
            exercise.name,
            emptyList(),
        )

        val workoutInstance = (workout.value as? DataState.Success<Workout>)?.data
        if (workoutInstance != null) {
            val exerciseSets = workoutInstance.sets.filter { it.exercise.name == exercise.name }
            val highWeight = exerciseSets.maxOf { it.weightInPounds }
            var nextSetNumber = exerciseSets.minOf { it.setNumberInWorkout }
            Timber.d("*** inserting warmup sets")

            val setsToAdd = arrayListOf(
                ExerciseSet(
                    0L,
                    exercise = exercise,
                    reps = 12,
                    setNumberInWorkout = nextSetNumber,
                    weightInPounds = if (highWeight > 100) 45 else (highWeight * .25).roundToInt(),
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
                        weightInPounds = round(warmupSet.key * highWeight, 5),
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

    private fun round(value: Double, nearest: Int): Int {
        return (value / nearest).roundToInt() * nearest
    }
}