package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.math.absoluteValue

@Composable
fun TimeSinceText(
    startTime: OffsetDateTime,
    totalTime: Long,
    modifier: Modifier = Modifier,
) {
    val initialTimeBetween = Duration.between(OffsetDateTime.now().toLocalDateTime(), startTime.toLocalDateTime())
    var seconds by rememberSaveable(startTime) { mutableStateOf(initialTimeBetween.seconds.absoluteValue) }

    LaunchedEffect(key1 = seconds) {
        if (seconds > 0) {
            delay(1000L)
            seconds -= 1
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            "${seconds / 60}:${if (seconds % 60 < 10) "0" else ""}${seconds % 60}",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )

        LinearProgressIndicator(
            progress = 1 - ((totalTime - seconds).toFloat() / totalTime.toFloat()),
            modifier = Modifier.fillMaxWidth()
        )
    }
}