package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.list

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.ExerciseSetType
import com.catscoffeeandkitchen.domain.models.Workout
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import java.time.OffsetDateTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorkoutSummaryCard(
    workout: Workout,
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
) {
    FitnessJournalCard(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onTap() },
                onLongClick = { onLongPress() },
            ),
        columnPaddingVertical = 4.dp
    ) {
        Text(workout.name, style = MaterialTheme.typography.headlineMedium)

        if (workout.completedAt == null) {

            Surface(
                modifier = Modifier.padding(vertical = 4.dp),
                shape = SuggestionChipDefaults.shape,
                tonalElevation = 4.dp
            ) {
                Text(
                    "In Progress",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        when (val completed = workout.completedAt) {
            null -> Text(
                "added " + DateUtils.getRelativeTimeSpanString(
                    workout.addedAt.toInstant().toEpochMilli(),
                    OffsetDateTime.now().toInstant().toEpochMilli(),
                    DateUtils.DAY_IN_MILLIS
                ).toString().lowercase(),
                style = MaterialTheme.typography.labelMedium
            )
            else -> Text(
                "finished " + DateUtils.getRelativeTimeSpanString(
                    completed.toInstant().toEpochMilli(),
                    OffsetDateTime.now().toInstant().toEpochMilli(),
                    DateUtils.DAY_IN_MILLIS
                ).toString().lowercase(),
                style = MaterialTheme.typography.labelMedium
            )
        }

        when (val plan = workout.plan) {
            null -> {}
            else -> {
                Text("plan: ${plan.name}")
                Text("${plan.entries.size} exercises")
            }
        }

        if (workout.note?.isNotEmpty() == true) {
            Text(
                workout.note!!,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        workout.sets.groupBy { it.exercise.name }.forEach { entry ->
            val warmupSets = entry.value.filter { it.type == ExerciseSetType.WarmUp }
            val workingSets = entry.value.filter { it.type == ExerciseSetType.Working }
            if (warmupSets.any()) {
                Text(
                    "${warmupSets.size}x${warmupSets.map { it.reps }.average().roundToInt()} " +
                            "${entry.key} Warm Up",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                "${workingSets.size}x${workingSets.map { it.reps }.average().roundToInt()} " +
                        entry.key,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
fun WorkoutCardPreview() {
    WorkoutSummaryCard(workout = Workout(
        name = "Best Workout Ever",
        addedAt = OffsetDateTime.now().minusDays(30L),
        completedAt = OffsetDateTime.now().minusDays(3),
        note = "A good workout for a nice burn"
    ))
}
