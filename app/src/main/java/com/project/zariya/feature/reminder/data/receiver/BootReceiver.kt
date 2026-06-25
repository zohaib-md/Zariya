package com.project.zariya.feature.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.project.zariya.feature.reminder.data.service.ReminderForegroundService
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that listens for BOOT_COMPLETED, QUICKBOOT_POWERON, and
 * MY_PACKAGE_REPLACED events. When triggered, it reschedules all active reminders
 * since AlarmManager alarms are cleared on device reboot.
 *
 * On Android 8+ (API 26+), starts a foreground service for reliable rescheduling.
 * On older versions, performs the rescheduling directly using goAsync().
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != "android.intent.action.QUICKBOOT_POWERON" &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.d(TAG, "Ignoring unrelated action: $action")
            return
        }

        Log.d(TAG, "Received boot/update event: $action")

        // Use foreground service for reliable rescheduling on modern Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val serviceIntent = ReminderForegroundService.createStartIntent(context)
                context.startForegroundService(serviceIntent)
                Log.d(TAG, "Started foreground service for rescheduling")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service, falling back to direct reschedule", e)
                rescheduleDirectly()
            }
        } else {
            rescheduleDirectly()
        }
    }

    private fun rescheduleDirectly() {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                alarmScheduler.rescheduleAllReminders()
                Log.d(TAG, "All reminders rescheduled directly from BootReceiver")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling reminders from BootReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
