package com.catscoffeeandkitchen.fitnessjournal.services

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class TimerServiceConnection: ServiceConnection {
    var timerService: TimerService? = null

    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        // We've bound to TimerService, cast the IBinder and get LocalService instance
        val binder = service as TimerService.LocalBinder
        timerService = binder.getService()
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
        timerService = null
    }
}
