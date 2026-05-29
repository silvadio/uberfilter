package com.driveq.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.driveq.service.AppForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AppForegroundService.start(context)
        }
    }
}
