package com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.catscoffeeandkitchen.fitnessjournal.services.TimerServiceConnection
import com.catscoffeeandkitchen.fitnessjournal.ui.workouts.details.exercise.TimeSinceText
import java.time.OffsetDateTime

@Composable
fun TimerSection(
    timeSinceKey: OffsetDateTime?,
    selectedTimer: Long,
    onUpdateTimeSinceKey: (OffsetDateTime) -> Unit,
    onUpdateSelectedTimer: (Long) -> Unit,
    startTimer: (Long) -> Unit = {},
    connection: TimerServiceConnection? = null,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val context = LocalContext.current
    var showNotificationRationaleDialog by remember { mutableStateOf(false) }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val seconds = connection?.timerService?.seconds
                if (seconds != null) {
                    onUpdateTimeSinceKey(OffsetDateTime.now().minusSeconds(seconds - 1))
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        startTimer(selectedTimer)
    }

    if (showNotificationRationaleDialog) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(.7f),
            onDismissRequest = { showNotificationRationaleDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationRationaleDialog = false
                        permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                    }
                ) { Text("OK") }
            },
            text = {
                Text("Notifications are only shown to" +
                        " display seconds left on a timer you start.")
            }
        )
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(12.dp)
            .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        Text("Start a Timer", style = MaterialTheme.typography.titleSmall)

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            listOf(30L, 60L, 90L, 120L).forEach { amount ->
                TimerButton(amount) { time ->
                    onUpdateTimeSinceKey(time)
                    onUpdateSelectedTimer(amount)

                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            "android.permission.POST_NOTIFICATIONS"
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            startTimer(amount)
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            context as Activity,
                            "android.permission.POST_NOTIFICATIONS"
                        ) -> {
                            showNotificationRationaleDialog = true
                        }
                        else -> {
                            permissionLauncher
                                .launch("android.permission.POST_NOTIFICATIONS")
                        }
                    }
                }
            }
        }

        timeSinceKey?.let { time ->
            TimeSinceText(
                startTime = time,
                totalTime = connection?.timerService?.startingSeconds ?: selectedTimer,
            )
        }
    }
}


@Composable
fun TimerButton(
    seconds: Long,
    onClick: (OffsetDateTime) -> Unit
) {
    TextButton(
        onClick = {
            onClick(OffsetDateTime.now().minusSeconds(seconds - 1))
        },
    ) {
        Text("${seconds}s")
    }
}
