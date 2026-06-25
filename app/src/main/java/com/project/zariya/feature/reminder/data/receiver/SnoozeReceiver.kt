package com.project.zariya.feature.reminder.data.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.project.zariya.feature.reminder.data.scheduler.AlarmSchedulerImpl
import com.project.zariya.feature.reminder.presentation.notification.ReminderNotificationBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BroadcastReceiver that handles snooze actions from reminder notifications.
 * When triggered:
 * 1. Schedules a new alarm for [snoozeMinutes] from now
 * 2. Dismisses the current notification
 *
 * The snoozed alarm will re-trigger [AlarmReceiver] which will show a fresh notification.
 */
@AndroidEntryPoint
class SnoozeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SnoozeReceiver"
        private const val DEFAULT_SNOOZE_MINUTES = 10
        private const val MILLIS_PER_MINUTE = 60 * 1000L
    }

    @Inject
    lateinit var notificationBuilder: ReminderNotificationBuilder

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(ReminderNotificationBuilder.EXTRA_REMINDER_ID)
        val snoozeMinutes = intent.getIntExtra(
            ReminderNotificationBuilder.EXTRA_SNOOZE_MINUTES,
            DEFAULT_SNOOZE_MINUTES
        )

        if (reminderId.isNullOrBlank()) {
            Log.e(TAG, "Received snooze with null or blank reminderId, ignoring")
            return
        }

        Log.d(TAG, "Snoozing reminder $reminderId for $snoozeMinutes minutes")

        // 1. Dismiss the current notification
        notificationBuilder.dismissNotification(reminderId)

        // 2. Schedule a new alarm for snoozeMinutes from now
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * MILLIS_PER_MINUTE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmSchedulerImpl.EXTRA_REMINDER_ID, reminderId)
            // Carry forward the original extras if available
            putExtra(
                AlarmSchedulerImpl.EXTRA_MEDICINE_NAME,
                intent.getStringExtra(AlarmSchedulerImpl.EXTRA_MEDICINE_NAME) ?: "Medicine"
            )
            putExtra(
                AlarmSchedulerImpl.EXTRA_DOSAGE,
                intent.getStringExtra(AlarmSchedulerImpl.EXTRA_DOSAGE) ?: ""
            )
            putExtra(
                AlarmSchedulerImpl.EXTRA_MEDICINE_FORM,
                intent.getStringExtra(AlarmSchedulerImpl.EXTRA_MEDICINE_FORM) ?: "TABLET"
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "SNOOZE").hashCode(),
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Snoozed alarm set for reminder $reminderId at $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling snooze alarm for $reminderId", e)
            // Fall back to inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
