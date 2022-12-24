package com.catscoffeeandkitchen.fitnessjournal.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.domain.models.ExerciseProgressStats
import com.catscoffeeandkitchen.domain.util.DataState
import com.catscoffeeandkitchen.fitnessjournal.R
import com.catscoffeeandkitchen.fitnessjournal.ui.util.toCleanString
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MostImprovedExerciseCardContent(
    state: DataState<ExerciseProgressStats?>
) {
    Text("Most improved exercise", style = MaterialTheme.typography.titleMedium)

    when (state) {
        is DataState.Loading -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        is DataState.Error -> {
            Text("Error = ${state.e.message}")
        }
        is DataState.Success -> {
            val stats = state.data

            if (stats == null) {
                Text(
                    "Not enough data to calculate this information.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text("over ${stats.amountOfTime.toDays() / 7} weeks", style = MaterialTheme.typography.labelMedium)
                Text(stats.exercise.name, style = MaterialTheme.typography.titleLarge)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(id = R.drawable.trending_up),
                        "Trending Up",
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        "${(stats.ending1RM - stats.starting1RM).roundToInt()}lbs (calculated 1RM)",
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    "${stats.bestSet.reps} reps " +
                            "@ ${stats.bestSet.weightInPounds.toCleanString()}lbs",
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    stats.bestSet.completedAt?.format(DateTimeFormatter.ofPattern("MMM dd yyyy")) ?: "",
                    style = MaterialTheme.typography.labelMedium
                )

                Text("${stats.worstSet.reps} reps " +
                        "@ ${stats.worstSet.weightInPounds.toCleanString()}lbs",
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    stats.worstSet.completedAt?.format(DateTimeFormatter.ofPattern("MMM dd yyyy")) ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        else -> { }
    }
}