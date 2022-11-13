package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import timber.log.Timber
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.math.absoluteValue

@Composable
fun TimeSinceText(
    startTime: OffsetDateTime,
    modifier: Modifier = Modifier,
) {
    val initialTimeBetween = Duration.between(OffsetDateTime.now().toLocalDateTime(), startTime.toLocalDateTime())
    var seconds by rememberSaveable(startTime) { mutableStateOf(initialTimeBetween.seconds.absoluteValue) }

    LaunchedEffect(key1 = seconds) {
        delay(1000L)
        seconds += 1
    }

    Text(
        "${seconds / 60}:${if (seconds % 60 < 10) "0" else ""}${seconds % 60}",
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primary)
            .padding(4.dp),
        color = MaterialTheme.colorScheme.onPrimary
    )
}