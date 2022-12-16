package com.catscoffeeandkitchen.fitnessjournal.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.catscoffeeandkitchen.fitnessjournal.MainActivity
import com.catscoffeeandkitchen.fitnessjournal.R

class TimerNotificationManager {
    companion object {
        const val notificationId = 1
    }

    fun createNotification(context: Context, seconds: Long, startingSeconds: Long): Notification {
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, TimerService.channelId)
            .setContentTitle(context.getString(R.string.x_seconds_on_timer, seconds.toString()))
            .setSmallIcon(R.drawable.fitness_center)
            .setContentIntent(pendingIntent)
            .setSilent(seconds > 0)
            .setProgress(startingSeconds.toInt(), seconds.toInt(), false)
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    fun updateNotification(context: Context, seconds: Long, startingSeconds: Long) {
        val notification = createNotification(context, seconds, startingSeconds)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(TimerService.notificationId, notification)
    }
}