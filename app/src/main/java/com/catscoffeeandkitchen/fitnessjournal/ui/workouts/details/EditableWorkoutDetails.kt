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
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import java.time.OffsetDateTime

@Composable
fun WorkoutDetails(
    scrollState: LazyListState,
    workout: Workout,
    unit: WeightUnit,
    updateWorkoutName: (String) -> Unit = {},
    updateWorkoutNote: (String?) -> Unit = {},
    addExercise: () -> Unit = {},
    addSet: (exercise: Exercise) -> Unit = {},
    addWarmupSets: (exercise: Exercise) -> Unit = {},
    removeExercise: (exercise: Exercise) -> Unit = {},
    removeSet: (set: ExerciseSet) -> Unit = { },
    updateExercise: (set: ExerciseSet, field: ExerciseSetField) -> Unit = { _, _ -> },
    swapExercise: (setNumber: Int, exercise: Exercise) -> Unit = { _, _, -> },
    createPlanFromWorkout: () -> Unit = { },
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

        if (workout.completedAt != null) {
            item {
                FitnessJournalButton(
                    text = "Create Plan from this Workout",
                    onClick = { createPlanFromWorkout() },
                    fullWidth = true
                )
            }
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
                    ExerciseUiData(
                        exercise,
                        sets = sets,
                        expectedSet = workout.plan?.exercises?.find { it.exercise.name == exercise.name },
                        unit = unit,
                    ),
                    addSet = addSet,
                    addWarmupSets = addWarmupSets,
                    removeSet = removeSet,
                    removeExercise = removeExercise,
                    updateExercise = updateExercise,
                    swapExercise = { swapExercise(sets.first().setNumberInWorkout, exercise) }
                )
            } else {
                ReadOnlyExerciseCard(
                    exercise = exercise,
                    sets = sets,
                    expectedSet = workout.plan?.exercises?.find { it.exercise.name == exercise.name },
                    unit = unit,
                    onEdit = { updateExercise(sets.last(), ExerciseSetField.Complete(null)) }
                )
            }
        }
        if (workout.completedAt == null) {
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
}

@Preview(showBackground = true)
@Composable
fun AddWorkoutFormPreview() {
    val workoutState = Workout(
            completedAt = OffsetDateTime.now().minusDays(3),
            addedAt = OffsetDateTime.now().minusDays(10),
            sets = listOf(
                ExerciseSet(
                    exercise = Exercise(
                        "Bicep Curls",
                        musclesWorked = listOf("Bicep", "Tricep"),
                    ),
                    reps = 10,
                    weightInPounds = 20f,
                    id = 20
                )
            )
        )

    val scrollState = rememberLazyListState()

    WorkoutDetails(
        workout = workoutState,
        scrollState = scrollState,
        unit = WeightUnit.Pounds
    )
}