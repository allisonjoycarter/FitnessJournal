package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.domain.usecases.*
import com.catscoffeeandkitchen.domain.usecases.exercise.*
import com.catscoffeeandkitchen.domain.usecases.exercisegroup.UpdateExercisesInGroupUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.AddExerciseSetUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.AddMultipleExerciseSetsUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.ReplaceExerciseForSetsUseCase
import com.catscoffeeandkitchen.domain.usecases.workoutplan.AddSetToWorkoutPlanUseCase
import com.catscoffeeandkitchen.domain.usecases.workoutplan.CreatePlanFromWorkoutUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.services.TimerService
import com.catscoffeeandkitchen.fitnessjournal.services.TimerServiceConnection
import com.catscoffeeandkitchen.fitnessjournal.ui.util.SharedPrefsConstants.WeightUnitKey
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit.Pounds
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.ExerciseUiActions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class WorkoutDetailsViewModel @Inject constructor(
    private val createWorkoutUseCase: CreateWorkoutUseCase,
    private val createPlanFromWorkoutUseCase: CreatePlanFromWorkoutUseCase,
    private val updateWorkoutUseCase: UpdateWorkoutUseCase,
    private val addSetUseCase: AddExerciseSetUseCase,
    private val addMultipleSetsUseCase: AddMultipleExerciseSetsUseCase,
    private val addEntryUseCase: AddWorkoutEntryUseCase,
    private val removeExerciseUseCase: RemoveExerciseFromWorkoutUseCase,
    private val replaceExerciseForSetsUseCase: ReplaceExerciseForSetsUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val updateMultipleSetsUseCase: UpdateMultipleSetsUseCase,
    private val updateExercisePositionUseCase: UpdateExercisePositionUseCase,
    private val updateExercisesInGroupUseCase: UpdateExercisesInGroupUseCase,
    private val chooseExerciseInGroupUseCase: ChooseExerciseInGroupUseCase,
    private val replaceExerciseWithGroup: ReplaceExerciseWithGroupUseCase,
    private val getWorkoutUseCase: GetWorkoutUseCase,
    private val removeSetUseCase: RemoveSetUseCase,
    private val sharedPreferences: SharedPreferences,
    val timerServiceConnection: TimerServiceConnection,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext val context: Context,
): ViewModel(), ExerciseUiActions {
    var cachedWorkout: Workout? = null
    private var connectionIsBound = false

    private var _workout: MutableState<DataState<Workout>> = mutableStateOf(DataState.NotSent())
    val workout: State<DataState<Workout>> = _workout

    private var _weightUnit: MutableState<WeightUnit> = mutableStateOf(Pounds)
    val weightUnit: State<WeightUnit> = _weightUnit

    private val workoutInstance: Workout?
        get() {
            val data = (_workout.value as? DataState.Success)?.data
            return if (data?.id == 0L) null
            else data
        }

    val exercises: List<UiExercise>
        get() = workoutInstance?.entries.orEmpty().map { entry ->
                    UiExercise(
                        name = entry.name,
                        position = entry.position,
                        exercise = entry.exercise ?: entry.expectedSet?.exercise,
                        group = entry.expectedSet?.exerciseGroup,
                    )
                }.sortedBy { it.position }

    val finishedExercises: List<UiExercise>
        get() = workoutInstance?.entries.orEmpty()
            .filter { it.sets.any { set -> set.isComplete } }
            .map { UiExercise(
                name = it.name,
                exercise = it.exercise,
                position = it.position
            ) }
            .sortedBy { it.position }

    val cachedFinishedExercises: List<UiExercise>
        get() = cachedWorkout?.entries.orEmpty()
            .filter { it.sets.any { set -> set.isComplete } }
            .map { UiExercise(
                name = it.name,
                exercise = it.exercise,
                position = it.position
            ) }
            .sortedBy { it.position }

    val cachedExercises: List<UiExercise>
        get() = cachedWorkout?.entries.orEmpty().map { entry ->
                    UiExercise(
                        name = entry.name,
                        position = entry.position,
                        exercise = entry.exercise ?: entry.expectedSet?.exercise,
                        group = entry.expectedSet?.exerciseGroup,
                    )
                }.sortedBy { it.position }

    init {
        val workoutId = savedStateHandle.get<Long>("workoutId")
        val plan = savedStateHandle.get<String?>("plan")
        if (workoutId != null) {
            if (workoutId == 0L && _workout.value is DataState.NotSent) {
                viewModelScope.launch {
                    createWorkoutUseCase.run(
                        Workout(id = 0L),
                        planId = plan?.toLong()
                    ).collect { state ->
                        _workout.value = state
                        if (state is DataState.Success) {
                            savedStateHandle["workoutId"] = state.data.id
                        }
                    }
                }
            } else {
                viewModelScope.launch {
                    getWorkoutUseCase.run(workoutId).collect { wo ->
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
                getWorkoutUseCase.run(currentWorkout.id).collect { wo ->
                    _workout.value = wo
                    cachedWorkout = (wo as? DataState.Success)?.data
                }
            }
        }
    }

    override fun swapExercise(
        exercisePosition: Int,
        exercise: Exercise,
    ) = viewModelScope.launch {
        val instance = workoutInstance
        if (instance != null) {
            val relatedSets = instance.entries
                .filter { it.position == exercisePosition }.flatMap { it.sets }

            replaceExerciseForSetsUseCase.run(
                relatedSets.map { it.id },
                exercise,
                exercisePosition,
                instance.addedAt
            ).collect { state ->
                if (state is DataState.Success) {
                    val updatedWorkout = instance.copy(
                        sets = instance.sets.map { set ->
                            if (relatedSets.any { it.id == set.id }) set.copy(exercise = state.data)
                            else set
                        }.sortedBy { it.setNumber }
                    )
                    _workout.value = DataState.Success(updatedWorkout)
                }
            }
        }
    }

    override fun addExercise(name: String) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { currentWorkout ->
            addEntryUseCase.run(
                entry = WorkoutEntry(
                    position = currentWorkout.entries.size + 1,
                    exercise = Exercise(name = name)
                ),
                workoutAddedAt = currentWorkout.addedAt,
            ).collect { state ->
                if (state is DataState.Success) {
                    workoutInstance?.let {
                        _workout.value = DataState.Success(
                            it.copy(entries = it.entries + state.data)
                        )
                    }
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
                    _workout.value = DataState.Success(updatedWorkout)
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

    fun createPlanFromWorkout() = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { wkot ->
            createPlanFromWorkoutUseCase.run(wkot).collect { dataState ->
                if (dataState is DataState.Success) {
                    getWorkout()
                }
            }
        }
    }

    override fun removeEntry(entry: WorkoutEntry) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { wkot ->
            removeExerciseUseCase.run(entry, wkot.addedAt).collect { state ->
                if (state is DataState.Success) {
                    getWorkout()
                }
            }
        }
    }

    override fun moveEntryTo(entry: WorkoutEntry, newPosition: Int) = viewModelScope.launch {
        workoutInstance?.let { instance ->
            updateExercisePositionUseCase.run(
                instance.addedAt,
                entry,
                newPosition,
            ).collect { result ->
                if (result is DataState.Success) {
                    getWorkout()
                }
            }
        }
    }

    override fun addExerciseSet(
        entry: WorkoutEntry,
        workoutId: Long
    ) = viewModelScope.launch {
        addSetUseCase.run(
            entry,
            workoutId = workoutId,
            set = ExerciseSet(
                0L,
                exercise = entry.exercise!!,
                reps = 0,
                setNumber = 0
            )
        ).collect { state ->
            if (state is DataState.Success) {
                (workout.value as? DataState.Success)?.data?.let { wkot ->
                    _workout.value = DataState.Success(
                        wkot.copy(entries = wkot.entries.map { entry ->
                            if (entry.position == state.data.position)
                                state.data
                            else
                                entry
                        })
                    )
                }
            }
        }
    }

    override fun updateSet(
        set: ExerciseSet,
    ) = viewModelScope.launch {
        updateSetUseCase.run(
            set = set,
        ).collect { state ->
            if (state is DataState.Success) {
                workoutInstance?.let { instance ->
                    _workout.value = DataState.Success(
                        instance.copy(entries = instance.entries.map { entry ->
                            entry.copy(sets = entry.sets.map { set ->
                                if (set.id == state.data.id) {
                                    state.data
                                } else {
                                    set
                                }
                            }.sortedBy { it.setNumber })
                        })
                    )
                }
            }
        }
    }

    override fun updateSets(sets: List<ExerciseSet>) = viewModelScope.launch {
        updateMultipleSetsUseCase.run(sets).collect { state ->
            if (state is DataState.Success) {
                workoutInstance?.let { instance ->
                    _workout.value = DataState.Success(
                        instance.copy(entries = instance.entries.map { entry ->
                            entry.copy(sets = entry.sets.map { set ->
                                val setToUse = sets.find { it.id == set.id }
                                Timber.d("*** setToUse = ${setToUse?.setNumber} (${setToUse?.id}), reps = ${setToUse?.reps}, pounds = ${setToUse?.weightInPounds}")
                                setToUse ?: set
                            }.sortedBy { it.setNumber })
                        })
                    )
                }
            }
        }
    }

    override fun removeSet(
        set: ExerciseSet,
        workoutId: Long,
    ) = viewModelScope.launch {
        removeSetUseCase.run(set, workoutId).collect { dataState ->
            if (dataState is DataState.Error) {
                Timber.e(dataState.e)
            }
            if (dataState is DataState.Success) {
                // filter out the set from the workout
                workoutInstance?.let { currentWorkout ->
                    _workout.value = DataState.Success(currentWorkout.copy(
                        entries = currentWorkout.entries.mapNotNull { entry ->
                            if (entry.sets.isNotEmpty() && entry.sets.all { it.id == set.id }) {
                                null
                            } else {
                                entry.copy(sets = entry.sets.filterNot { s -> s.id == set.id }
                                    .sortedBy { it.setNumber })
                            }
                        }
                    )
                )}
            }
        }
    }

    override fun addWarmupSets(
        workoutId: Long,
        entry: WorkoutEntry,
        unit: WeightUnit
    ) = viewModelScope.launch {
        val highWeight = if (unit == WeightUnit.Pounds)
            entry.sets.maxOf { it.weightInPounds } else
            entry.sets.maxOf { it.weightInKilograms }

        val increments = mapOf(
            .25 to 12,
            .575 to 8,
            .725 to 5,
            .825 to 3,
            .925 to 1
        )

        var nextSetNumber = 1
        val setsToAdd = arrayListOf<ExerciseSet>()
        increments.forEach { warmupSet ->
            setsToAdd.add(
                ExerciseSet(
                    0L,
                    exercise = entry.exercise ?: entry.expectedSet?.exercise ?: Exercise(name = "Unknown Exercise"),
                    reps = warmupSet.value,
                    setNumber = nextSetNumber,
                    weightInPounds = round(warmupSet.key * highWeight, 5).toFloat(),
                    weightInKilograms = round(warmupSet.key * highWeight, 5).toFloat(),
                    type = ExerciseSetType.WarmUp
                )
            )
            nextSetNumber++
        }

        addMultipleSetsUseCase.run(
            entry = entry,
            workoutId = workoutId,
            sets = setsToAdd
        ).collect { state ->
            if (state is DataState.Success) {
                workoutInstance?.let { wo ->
                    _workout.value = DataState.Success(
                        wo.copy(entries = wo.entries.map { item ->
                            if (entry.position == item.position) state.data.copy(
                                sets = state.data.sets.sortedBy { it.setNumber })
                            else item
                        })
                    )}
            }
        }
    }

    override fun replaceWithGroup(entry: WorkoutEntry): Job = viewModelScope.launch {
        workoutInstance?.let { wo ->
            replaceExerciseWithGroup.run(wo.addedAt, entry).collect { state ->
                if (state is DataState.Success) {
                    _workout.value = DataState.Success(wo.copy(entries = wo.entries.map { item ->
                        if (item.position == entry.position) {
                            state.data
                        } else {
                            item
                        }
                    }))
                }
            }
        }
    }

    override fun selectExerciseFromGroup(
        group: ExerciseGroup,
        exercise: Exercise,
        position: Int,
        expectedSet: ExpectedSet?,
    ) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { workout ->
            chooseExerciseInGroupUseCase
                .run(workout.addedAt, group, exercise, position, expectedSet)
                .collect { result ->
                    if (result is DataState.Success) {
                        getWorkout()
                    } else if (result is DataState.Error) {
                        Timber.e(result.e)
                    }
            }
        }
    }

    fun editGroup(
        exerciseGroup: ExerciseGroup,
        selectedExercises: List<String>,
    ) = viewModelScope.launch {
        updateExercisesInGroupUseCase.run(exerciseGroup, selectedExercises).collect { state ->
            if (state is DataState.Success) {
                workoutInstance?.let { _workout.value = DataState.Success(it.copy(
                    entries = it.entries.map { entry ->
                        if (entry.expectedSet?.exerciseGroup?.id == exerciseGroup.id) {
                            entry.copy(
                                expectedSet = entry.expectedSet?.copy(
                                    exerciseGroup = state.data
                                )
                            )
                        } else {
                            entry
                        }
                    }
                )) }
            }
        }
    }

    private fun round(value: Double, nearest: Int): Int {
        return (value / nearest).roundToInt() * nearest
    }

    fun startTimerNotification(seconds: Long) {
        timerServiceConnection.timerService?.cancelTimer()

        val intent = Intent(context, TimerService::class.java).apply {
            putExtra("seconds", seconds)
            putExtra("workoutId", workoutInstance?.id)
        }

        context.startForegroundService(intent)
        connectionIsBound = context.bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onCleared() {
        if (connectionIsBound) {
            context.unbindService(timerServiceConnection)
        }
        super.onCleared()
    }
}