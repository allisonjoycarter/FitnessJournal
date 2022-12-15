package com.catscoffeeandkitchen.fitnessjournal.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.catscoffeeandkitchen.fitnessjournal.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class TimerService: LifecycleService() {
    private val binder = LocalBinder()
    var seconds = 0L
    var startingSeconds = 0L
    private var timerJob: Job = Job()

    companion object {
        const val channelId = "TimerChannel"
        const val notificationId = 1
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    fun cancelTimer() {
        Timber.d("*** cancelling previous timer...")
        seconds = 0L
        timerJob.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val channel = NotificationChannel(channelId, "Timer", NotificationManager.IMPORTANCE_NONE)
        channel.description = "Keep the timers running while you're away from the app."

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(channelId, "Timer", NotificationManager.IMPORTANCE_DEFAULT))

        val secondsInIntent = intent?.getLongExtra("seconds", 30L) ?: 30L
        seconds = secondsInIntent
        startingSeconds = secondsInIntent

        val notificationManager = TimerNotificationManager()
        val notification = notificationManager.createNotification(this, secondsInIntent)

        startForeground(notificationId, notification)

        timerJob = lifecycleScope.launch(Dispatchers.IO) {
            (0L..secondsInIntent)
                .asFlow()
                .onEach { delay(1_000) }
                .collect { sec ->
                    val remaining = secondsInIntent - sec
                    seconds = remaining

                    notificationManager.updateNotification(this@TimerService, remaining)
                    if (remaining == 0L) {
                        val player = MediaPlayer.create(this@TimerService, R.raw.alert_chime)
                        player.start()
                        this@TimerService.stopSelf()
                    }
                }
        }

        return START_STICKY
    }
}
