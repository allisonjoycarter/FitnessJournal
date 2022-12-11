package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.*
import java.time.OffsetDateTime

@Composable
fun WorkoutDetails(
    scrollState: LazyListState,
    workout: Workout,
    unit: WeightUnit,
    workoutActions: WorkoutActions?,
    exerciseUiActions: ExerciseUiActions?,
    exerciseNavigableActions: ExerciseNavigableActions?,
) {
    var editingExercise by remember { mutableStateOf(null as String?) }
    var useKeyboard by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            WorkoutNameAndNoteSection(
                workoutName = workout.name,
                workoutNote = workout.note,
                updateName = { workoutActions?.updateName(it) },
                updateNote = { workoutActions?.updateNote(it) },
            )
        }

        if (workout.completedAt != null) {
            item {
                FitnessJournalButton(
                    text = "Create Plan from this Workout",
                    onClick = { workoutActions?.createPlanFromWorkout() },
                    fullWidth = true
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    useKeyboard,
                    onCheckedChange = { useKeyboard = !useKeyboard }
                )
                Text("Use Keyboard", style = MaterialTheme.typography.labelMedium)
            }
        }

        val listOfExercises: List<UiExercise> = (
                (workout.sets
                    .sortedBy { (it.exercise.positionInWorkout ?: 0) }
                    .map { UiExercise(
                        uniqueIdentifier = it.exercise.name,
                        name = it.exercise.name,
                        exercise = it.exercise,
                        position = it.exercise.positionInWorkout ?: 1
                    ) })
                        +
                (workout.plan?.exercises.orEmpty()
                    .sortedBy { it.positionInWorkout }
                    .filter { workout.sets.none { set ->
                        set.exercise.positionInWorkout == it.positionInWorkout } }
                    .map { UiExercise(
                        uniqueIdentifier = it.exercise?.name ?: "${it.exerciseGroup?.id}${it.positionInWorkout}",
                        name = it.exercise?.name ?: it.exerciseGroup?.name ?: "Unknown Exercise",
                        exercise = it.exercise,
                        group = it.exerciseGroup,
                        position = it.positionInWorkout
                    ) })
                )
            .distinctBy { it.uniqueIdentifier }

        itemsIndexed(listOfExercises) { index, exercise ->
            val sets = workout.sets
                .filter { s -> s.exercise.name == exercise.name }
                .sortedBy { it.setNumber }
            if (exercise.exercise != null &&
                (editingExercise == exercise.name ||
                workout.sets.filter { it.exercise.name == exercise.name }.any { !it.isComplete })
            ) {
                InProgressExerciseCard(
                    ExerciseUiData(
                        workoutAddedAt = workout.addedAt,
                        exercise = exercise.exercise,
                        sets = sets,
                        expectedSet = workout.plan?.exercises?.find { it.positionInWorkout == exercise.position },
                        unit = unit,
                        isFirstExercise = index == 0,
                        isLastExercise = index == listOfExercises.lastIndex,
                        useKeyboard = useKeyboard,
                    ),
                    uiActions = exerciseUiActions,
                    navigableActions = exerciseNavigableActions
                )
            } else if (exercise.exercise != null) {
                ReadOnlyExerciseCard(
                    exercise = exercise.exercise,
                    sets = sets,
                    expectedSet = workout.plan?.exercises?.find { it.exercise?.name == exercise.name },
                    unit = unit,
                    onEdit = { editingExercise = exercise.name }
                )
            } else if (exercise.group != null) {
                ExerciseGroupCard(
                    exercise.group,
                    onExerciseSelected = { ex ->
                        exerciseUiActions?.selectExerciseFromGroup(
                            exercise.group,
                            ex,
                            ex.positionInWorkout ?: 1,
                            expectedSet = workout.plan?.exercises?.firstOrNull { it.positionInWorkout == ex.positionInWorkout })
                    },
                    editGroup = {
                        exerciseNavigableActions?.editGroup(exercise.group)
                    }
                )
            }
        }

        if (workout.completedAt == null) {
            item {
                FitnessJournalButton(
                    "Add Exercise",
                    onClick = {
                        exerciseNavigableActions?.addExercise()
                    },
                    fullWidth = true,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    OutlinedButton(
                        onClick = { workoutActions?.finish() },
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
        unit = WeightUnit.Pounds,
        workoutActions = null,
        exerciseUiActions = null,
        exerciseNavigableActions = null,
    )
}