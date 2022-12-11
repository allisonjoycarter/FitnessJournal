package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.plan.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.WorkoutPlan
import com.catscoffeeandkitchen.fitnessjournal.ui.components.FitnessJournalCard
import java.time.OffsetDateTime

@Composable
fun WorkoutPlanSummaryCard(
    workout: WorkoutPlan,
    onTap: () -> Unit = {}
) {
    FitnessJournalCard(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                onTap()
            }
    ) {
        Text(workout.name, style = MaterialTheme.typography.headlineMedium)

        if (workout.note?.isNotEmpty() == true) {
            Text(
                workout.note!!,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Text(
            workout.exercises.size.toString() + " exercises",
            style = MaterialTheme.typography.labelLarge
        )

        val musclesWorkedDescriptor = MusclesWorkedDescriptor(workout.exercises)

        if (musclesWorkedDescriptor.mostCommonMuscleWorked?.trim()?.isNotEmpty() == true) {
            Text(
                "${musclesWorkedDescriptor.mostCommonMuscleWorked} focused",
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (musclesWorkedDescriptor.compoundMovements.isNotEmpty()) {
            Text(
                "includes " + musclesWorkedDescriptor.compoundMovements,
                style = MaterialTheme.typography.labelLarge
            )
        }

        workout.exercises.forEach { set ->
            val exerciseName = set.exercise?.name ?: set.exerciseGroup?.name ?: set.note.ifEmpty { " Unknown Exercise" }
            Text(
                "${set.sets}x${set.reps} $exerciseName",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(4.dp)
            )
        }
    }
}

@Preview
@Composable
fun WorkoutCardPreview() {
    WorkoutPlanSummaryCard(workout = WorkoutPlan(
        name = "Best Workout Ever",
        addedAt = OffsetDateTime.now().minusDays(30L),
        note = "A good workout for a nice burn"
    ))
}
