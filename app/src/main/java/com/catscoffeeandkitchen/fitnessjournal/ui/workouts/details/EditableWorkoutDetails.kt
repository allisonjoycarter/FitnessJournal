package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.CurrentExerciseCard
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.ExerciseSetField
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.currentworkout.ReadOnlyExerciseCard
import java.time.OffsetDateTime

@Composable
fun EditableWorkoutDetails(
    workout: State<DataState<Workout>>,
    modifier: Modifier = Modifier,
    cachedWorkout: Workout? = null,
    updateWorkoutName: (String) -> Unit = {},
    updateWorkoutNote: (String?) -> Unit = {},
    addExercise: () -> Unit = { },
    addSet: (exercise: Exercise) -> Unit = {},
    addWarmupSets: (exercise: Exercise) -> Unit = {},
    removeSet: (exerciseSet: ExerciseSet) -> Unit = { },
    removeExercise: (exercise: Exercise) -> Unit = { },
    updateExercise: (set: ExerciseSet, field: ExerciseSetField, value: Int) -> Unit = {  _, _, _ -> },
    finish: () -> Unit = {},
    ) {
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
    ) {
        when (val workoutState = workout.value) {
            is DataState.NotSent -> {}
            is DataState.Loading -> {
                if (cachedWorkout != null) {
                    WorkoutDetails(
                        listState,
                        workout = cachedWorkout,
                    )
                } else {
                    CircularProgressIndicator()
                }
            }
            is DataState.Success -> {
                WorkoutDetails(
                    listState,
                    workout = workoutState.data,
                    updateWorkoutName = updateWorkoutName,
                    updateWorkoutNote = updateWorkoutNote,
                    addExercise = addExercise,
                    addSet = addSet,
                    addWarmupSets = addWarmupSets,
                    removeSet = removeSet,
                    updateExercise = updateExercise,
                    removeExercise = removeExercise,
                    finish = finish,
                )
            }
            is DataState.Error -> {
                Text("Error = ${workoutState.e.localizedMessage}")
            }
            else -> {
                Text(text = "State = $workoutState")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetails(
    scrollState: LazyListState,
    workout: Workout,
    updateWorkoutName: (String) -> Unit = {},
    updateWorkoutNote: (String?) -> Unit = {},
    addExercise: () -> Unit = {},
    addSet: (exercise: Exercise) -> Unit = {},
    addWarmupSets: (exercise: Exercise) -> Unit = {},
    removeExercise: (exercise: Exercise) -> Unit = {},
    removeSet: (set: ExerciseSet) -> Unit = { },
    updateExercise: (set: ExerciseSet, field: ExerciseSetField, value: Int) -> Unit = { _, _, _ -> },
    finish: () -> Unit = {}
) {

    LazyColumn(
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            WorkoutNameAndNoteSection(
                workoutName = workout.name,
                workoutNote = workout.note,
                updateName = { updateWorkoutName(it) },
                updateNote = { updateWorkoutNote(it) },
            )
        }

        val listOfExercises = (
                (workout.sets
                    .sortedBy { it.setNumberInWorkout }
                    .map { it.exercise })
                        +
                (workout.plan?.exercises.orEmpty()
                    .sortedBy { it.setNumberInWorkout }
                    .map { it.exercise })
                )
            .distinctBy { it.name }

        items(listOfExercises) { exercise ->
            val sets = workout.sets
                .filter { s -> s.exercise.name == exercise.name }
                .sortedBy { it.setNumberInWorkout }
            if (workout.sets.filter { it.exercise.name == exercise.name }.any { !it.isComplete }) {
                CurrentExerciseCard(
                    exercise,
                    sets = sets,
                    expectedSet = workout.plan?.exercises?.find { it.exercise.name == exercise.name },
                    useKeyboardForEntry = true,
                    addSet = addSet,
                    addWarmupSets = addWarmupSets,
                    removeSet = removeSet,
                    removeExercise = removeExercise,
                    updateExercise = updateExercise,
                )
            } else {
                ReadOnlyExerciseCard(
                    exercise = exercise,
                    sets = sets,
                    expectedSet = workout.plan?.exercises?.find { it.exercise.name == exercise.name },
                    onEdit = { updateExercise(sets.last(), ExerciseSetField.Complete, 0) }
                )
            }
        }
        item {
            FitnessJournalButton(
                "Add Exercise",
                onClick = {
                    addExercise()
                },
                fullWidth = true,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                OutlinedButton(
                    onClick = { finish() },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Finish Workout")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddWorkoutFormPreview() {
    val workoutState = remember {
        mutableStateOf(DataState.Success(Workout(
            completedAt = OffsetDateTime.now().minusDays(3),
            addedAt = OffsetDateTime.now().minusDays(10),
            sets = listOf(
                ExerciseSet(
                    exercise = Exercise(
                        "Bicep Curls",
                        musclesWorked = listOf("Bicep", "Tricep"),
                    ),
                    reps = 10,
                    weightInPounds = 20,
                    id = 20
                )
            )
        )))
    }

    EditableWorkoutDetails(
        workoutState
    )
}