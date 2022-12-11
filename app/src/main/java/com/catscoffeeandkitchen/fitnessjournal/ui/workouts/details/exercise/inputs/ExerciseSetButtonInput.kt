package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.inputs

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.catscoffeeandkitchen.fitnessjournal.ui.theme.FitnessJournalTheme


@Composable
fun ExerciseSetButtonInput(
    value: String,
    label: String,
    labelColor: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Text(
                value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor
        )
    }
}

@Preview(
    showBackground = false,
)
@Composable
fun SetButtonPreview() {
    FitnessJournalTheme() {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            for (i in 0 until 4) {
                ExerciseSetButtonInput(
                    value = (i + 5).toString(),
                    label = "reps",
                    labelColor = MaterialTheme.colorScheme.onSurface) {
                }
            }
        }
    }
}
