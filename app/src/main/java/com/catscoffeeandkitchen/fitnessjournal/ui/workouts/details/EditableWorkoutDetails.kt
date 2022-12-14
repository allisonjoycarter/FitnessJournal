package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.ui.components.AddExerciseOrGroupButtons
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.*
import java.time.OffsetDateTime

@Composable
fun WorkoutDetails(
    scrollState: LazyListState,
    workout: Workout,
    sets: List<UiExercise>,
    unit: WeightUnit,
    workoutActions: WorkoutActions?,
    exerciseUiActions: ExerciseUiActions?,
    exerciseNavigableActions: ExerciseNavigableActions?,
) {
    var editingExercise by remember { mutableStateOf(null as String?) }
    var useKeyboard by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
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

        if (editingExercise != null || workout.sets.any { !it.isComplete }) {
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
                    Text("Use Keyboard Inputs", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        itemsIndexed(sets) { index, exercise ->
            val individualSets = workout.sets
                .filter { s -> s.exercise.name == exercise.name }
                .sortedBy { it.setNumber }
            if (exercise.exercise != null &&
                (editingExercise == exercise.name ||
                individualSets.any { !it.isComplete })
            ) {
                EditableExerciseCard(
                    ExerciseUiData(
                        workoutAddedAt = workout.addedAt,
                        exercise = exercise.exercise,
                        sets = individualSets,
                        expectedSet = workout.plan?.exercises?.find { it.positionInWorkout == exercise.position },
                        unit = unit,
                        isFirstExercise = index == 0,
                        isLastExercise = index == sets.lastIndex,
                        useKeyboard = useKeyboard,
                        wasChosenFromGroup = workout.plan?.exercises?.any { plannedExercise ->
                            plannedExercise.exerciseGroup != null &&
                                    plannedExercise.exerciseGroup!!.exercises.any { it.name == exercise.name } &&
                                    plannedExercise.positionInWorkout == exercise.position
                        } == true
                    ),
                    uiActions = exerciseUiActions,
                    navigableActions = exerciseNavigableActions
                )
            } else if (exercise.exercise != null) {
                ReadOnlyExerciseCard(
                    exercise = exercise.exercise,
                    sets = individualSets,
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
                    icon = {
                        Icon(painterResource(id = R.drawable.fitness_center), "group")
                    },
                    fullWidth = true,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { workoutActions?.finish() },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text("Finish Workout")
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
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
        sets = workoutState.sets.map { set ->
            UiExercise(
                uniqueIdentifier = set.exercise.name,
                name = set.exercise.name,
                exercise = set.exercise,
                group = null,
                position = 1
            )
        },
        scrollState = scrollState,
        unit = WeightUnit.Pounds,
        workoutActions = null,
        exerciseUiActions = null,
        exerciseNavigableActions = null,
    )
}