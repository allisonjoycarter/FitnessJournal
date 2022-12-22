package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.Exercise
import com.catscoffeeandkitchen.domain.models.ExerciseSet
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.TestTags
import com.catscoffeeandkitchen.fitnessjournal.services.TimerServiceConnection
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalButton
import com.catscoffeeandkitchen.fitnessjournal.ui.util.WeightUnit
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.*
import java.time.OffsetDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutDetails(
    scrollState: LazyListState,
    workout: Workout,
    sets: List<UiExercise>,
    unit: WeightUnit,
    workoutActions: WorkoutActions?,
    exerciseUiActions: ExerciseUiActions?,
    exerciseNavigableActions: ExerciseNavigableActions?,
    startTimer: (Long) -> Unit = {},
    connection: TimerServiceConnection? = null,
) {
    var useKeyboard by rememberSaveable { mutableStateOf(false) }
    val (startTimerOnSetFinish, setTimerOnStartFinish) = rememberSaveable { mutableStateOf(false) }
    val (timeSinceKey, setTimeSinceKey) = remember { mutableStateOf(null as OffsetDateTime?) }
    val (selectedTimer, setSelectedTimer) = remember { mutableStateOf(30L) }
    var showSettings by remember { mutableStateOf(false) }

    LazyColumn(
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag(TestTags.ScrollableComponent),
    ) {
        item {
            WorkoutNameAndNoteSection(
                workoutName = workout.name,
                workoutNote = workout.note,
                updateName = { workoutActions?.updateName(it) },
                updateNote = { workoutActions?.updateNote(it) },
            )
        }

        item {
            TextButton(
                modifier = Modifier.padding(start = 8.dp).animateItemPlacement(),
                onClick = { showSettings = !showSettings }
            ) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    "toggle options section",
                    modifier = Modifier.rotate(if (showSettings) 180f else 0f)
                )
                Text("${if (showSettings) "hide" else "show"} options")
            }
        }

        val showKeyboardSwitch = showSettings && workout.entries.any { it.sets.any { set -> !set.isComplete } }
        item {
            AnimatedVisibility(
                showKeyboardSwitch,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
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

        item {
            AnimatedVisibility(
                visible = showSettings,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = startTimerOnSetFinish,
                        onCheckedChange = { setTimerOnStartFinish(it) }
                    )
                    Text("Start timer after set", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        val showCreatePlan = showSettings && workout.completedAt != null
        item {
            AnimatedVisibility(
                visible = showCreatePlan,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FitnessJournalButton(
                    text = "Create Plan from this Workout",
                    onClick = { workoutActions?.createPlanFromWorkout() },
                    fullWidth = true,
                )
            }
        }

        if (workout.completedAt == null) {
            stickyHeader {
                TimerSection(
                    timeSinceKey,
                    selectedTimer,
                    onUpdateTimeSinceKey = setTimeSinceKey,
                    onUpdateSelectedTimer = setSelectedTimer,
                    startTimer = startTimer,
                    connection = connection
                )
            }
        }

        itemsIndexed(workout.entries, key = { _, entry -> entry.position }) { index, entry ->
            ExerciseCard(
                ExerciseUiData(
                    workout.id,
                    entry,
                    unit = unit,
                    isFirstExercise = index == 0,
                    isLastExercise = index == sets.lastIndex,
                    useKeyboard = useKeyboard,
                    wasChosenFromGroup = entry.exercise != null &&
                            entry.expectedSet?.exerciseGroup != null,
                ),
                uiActions = exerciseUiActions,
                navigableActions = exerciseNavigableActions,
                onCompleteSet = { time ->
                    if (startTimerOnSetFinish && time != null) {
                        startTimer(selectedTimer)
                        setTimeSinceKey(time.minusSeconds(selectedTimer - 1))
                    }
                },
                modifier = Modifier.animateItemPlacement(),
            )
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
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .testTag(TestTags.AddExerciseButton)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
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
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddWorkoutFormPreview() {
    val workoutState = Workout(
        id = 1L,
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