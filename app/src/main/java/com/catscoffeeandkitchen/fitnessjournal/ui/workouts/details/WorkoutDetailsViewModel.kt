package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.domain.usecases.*
import com.catscoffeeandkitchen.domain.usecases.exercise.ChooseExerciseInGroupUseCase
import com.catscoffeeandkitchen.domain.usecases.exercise.RemoveExerciseFromWorkoutUseCase
import com.catscoffeeandkitchen.domain.usecases.exercise.ReplaceExerciseWithGroupUseCase
import com.catscoffeeandkitchen.domain.usecases.exercise.UpdateExercisePositionUseCase
import com.catscoffeeandkitchen.domain.usecases.exercisegroup.UpdateExercisesInGroupUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.AddExerciseSetUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.AddMultipleExerciseSetsUseCase
import com.catscoffeeandkitchen.domain.usecases.exerciseset.ReplaceExerciseForSetsUseCase
import com.catscoffeeandkitchen.domain.usecases.workoutplan.AddSetToWorkoutPlanUseCase
import com.catscoffeeandkitchen.domain.usecases.workoutplan.CreatePlanFromWorkoutUseCase
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.util.SharedPrefsConstants.WeightUnitKey
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit.Pounds
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.ExerciseUiActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
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
    private val addSetToWorkoutPlan: AddSetToWorkoutPlanUseCase,
    private val addMultipleSetsUseCase: AddMultipleExerciseSetsUseCase,
    private val removeExerciseUseCase: RemoveExerciseFromWorkoutUseCase,
    private val replaceExerciseForSetsUseCase: ReplaceExerciseForSetsUseCase,
    private val updateSetUseCase: UpdateSetUseCase,
    private val updateExercisePositionUseCase: UpdateExercisePositionUseCase,
    private val updateExercisesInGroupUseCase: UpdateExercisesInGroupUseCase,
    private val chooseExerciseInGroupUseCase: ChooseExerciseInGroupUseCase,
    private val replaceExerciseWithGroup: ReplaceExerciseWithGroupUseCase,
    private val getWorkoutByAddedDateUseCase: GetWorkoutByAddedDateUseCase,
    private val removeSetUseCase: RemoveSetUseCase,
    private val sharedPreferences: SharedPreferences,
    savedStateHandle: SavedStateHandle,
): ViewModel(), ExerciseUiActions {
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

    val exercises: List<UiExercise>
        get() = (
                workoutInstance?.sets.orEmpty().map { UiExercise(
                        uniqueIdentifier = "${it.exercise.name}${it.exercise.positionInWorkout}",
                        name = it.exercise.name,
                        exercise = it.exercise,
                        position = it.exercise.positionInWorkout ?: 1
                    ) } +
                workoutInstance?.plan?.exercises.orEmpty()
                    .filter { workoutInstance?.sets.orEmpty().none { set ->
                        set.exercise.positionInWorkout == it.positionInWorkout } }
                    .map { UiExercise(
                        uniqueIdentifier = if (it.exercise != null)
                            "${it.exercise?.name}${it.exercise?.positionInWorkout}" else
                                "${it.exerciseGroup?.id}${it.positionInWorkout}",
                        name = it.exercise?.name ?: it.exerciseGroup?.name ?: "Unknown Exercise",
                        exercise = it.exercise,
                        group = it.exerciseGroup,
                        position = it.positionInWorkout
                    ) }
                ).sortedBy { it.position }.distinctBy { it.uniqueIdentifier }

    val finishedExercises: List<UiExercise>
        get() = workoutInstance?.sets.orEmpty()
            .filter { it.isComplete }
            .map { UiExercise(
                uniqueIdentifier = "${it.exercise.name}${it.exercise.positionInWorkout}",
                name = it.exercise.name,
                exercise = it.exercise,
                position = it.exercise.positionInWorkout ?: 1
            ) }
            .sortedBy { (it.exercise?.positionInWorkout ?: 0) }
            .distinctBy { it.uniqueIdentifier }

    val cachedFinishedExercises: List<UiExercise>
        get() = cachedWorkout?.sets.orEmpty()
            .map { UiExercise(
                uniqueIdentifier = it.exercise.name,
                name = it.exercise.name,
                exercise = it.exercise,
                position = it.exercise.positionInWorkout ?: 1
            ) }
            .sortedBy { (it.exercise?.positionInWorkout ?: 0) }
            .distinctBy { it.uniqueIdentifier }

    val cachedExercises: List<UiExercise>
        get() = (
                cachedWorkout?.sets.orEmpty().map { UiExercise(
                    uniqueIdentifier = it.exercise.name,
                    name = it.exercise.name,
                    exercise = it.exercise,
                    position = it.exercise.positionInWorkout ?: 1
                ) } +
                        cachedWorkout?.plan?.exercises.orEmpty()
                            .filter { workoutInstance?.sets.orEmpty().none { set ->
                                set.exercise.positionInWorkout == it.positionInWorkout } }
                            .map { UiExercise(
                                uniqueIdentifier = it.exercise?.name ?: "${it.exerciseGroup?.id}${it.positionInWorkout}",
                                name = it.exercise?.name ?: it.exerciseGroup?.name ?: "Unknown Exercise",
                                exercise = it.exercise,
                                group = it.exerciseGroup,
                                position = it.positionInWorkout
                            ) }
                ).sortedBy { it.position }.distinctBy { it.uniqueIdentifier }

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
                    Timber.d("*** workout sets = ${(wo as? DataState.Success)?.data}")
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
            // TODO: only remove group of sets, not all instances of that exercise
            val relatedSets = instance.sets
                .filter { it.exercise.positionInWorkout == exercisePosition }

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
                        }
                    )
                    _workout.value = DataState.Success(updatedWorkout)
                }
            }
        }
    }

    override fun addExercise(name: String) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { currentWorkout ->
            addSetUseCase.run(
                set = ExerciseSet(
                    id = 0L,
                    reps = 0,
                    exercise = Exercise(name = name),
                    setNumber = 0,
                ),
                exerciseName = name,
                workoutAddedAt = currentWorkout.addedAt,
            ).collect { state ->
                if (state is DataState.Success) {
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
                _createdPlanAddedAt.value = dataState
            }
        }
    }

    override fun removeExercise(exercise: Exercise) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { wkot ->
            removeExerciseUseCase.run(exercise, wkot.addedAt).collect { state ->
                if (state is DataState.Success) {
                    _workout.value = DataState.Success(
                        wkot.copy(sets = wkot.sets.filter { it.exercise.name != exercise.name })
                    )
                }
            }
        }
    }

    override fun moveExerciseTo(exercise: Exercise, newPosition: Int) = viewModelScope.launch {
        (workout.value as? DataState.Success)?.data?.let { workout ->
            updateExercisePositionUseCase.run(
                workout.addedAt,
                exercise,
                newPosition,
            ).collect { result ->
                if (result is DataState.Success) {
                    getWorkout()
                }
            }
        }
    }

    override fun addExerciseSet(
        name: String,
        workoutAddedAt: OffsetDateTime
    ) = viewModelScope.launch {
        val exercise = Exercise(
            name,
        )

        addSetUseCase.run(
            exerciseName = name,
            workoutAddedAt = workoutAddedAt,
            set = ExerciseSet(
                0L,
                exercise = exercise,
                reps = 0,
                setNumber = 0
            )
        ).collect { state ->
            if (state is DataState.Success) {
                (workout.value as? DataState.Success)?.data?.let { wkot ->
                    _workout.value = DataState.Success(
                        wkot.copy(sets = wkot.sets + state.data)
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
                (workout.value as? DataState.Success)?.data?.let { wkot ->
                    _workout.value = DataState.Loading()
                    _workout.value = DataState.Success(
                        wkot.copy(sets = wkot.sets.map { set ->
                            if (set.id == state.data.id) {
                                state.data
                            } else {
                                set
                            }
                        })
                    )
                }
            }
        }
    }

    override fun removeSet(
        setId: Long
    ) = viewModelScope.launch {
        removeSetUseCase.run(setId).collect { dataState ->
            Timber.d("removing set $dataState")
            if (dataState is DataState.Error) {
                Timber.e(dataState.e)
            }
            if (dataState is DataState.Success) {
                Timber.d("removed set")
            }
        }
    }

    override fun addWarmupSets(
        workoutAddedAt: OffsetDateTime,
        exercise: Exercise,
        sets: List<ExerciseSet>,
        unit: WeightUnit
    ) = viewModelScope.launch {
        val highWeight = if (unit == WeightUnit.Pounds)
            sets.maxOf { it.weightInPounds } else
            sets.maxOf { it.weightInKilograms }

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
                    exercise = exercise,
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
            exercise = exercise,
            workoutAddedAt = workoutAddedAt,
            sets = setsToAdd
        ).collect { state ->
            if (state is DataState.Success) {
                getWorkout()
            }
        }
    }

    override fun replaceWithGroup(exercisePosition: Int, exercise: Exercise): Job = viewModelScope.launch {
        workoutInstance?.addedAt?.let { addedAt ->
            replaceExerciseWithGroup.run(addedAt, exercise, exercisePosition).collect { state ->
                if (state is DataState.Success) {
                    getWorkout()
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
                getWorkout()
            }
        }
    }

    private fun round(value: Double, nearest: Int): Int {
        return (value / nearest).roundToInt() * nearest
    }
}