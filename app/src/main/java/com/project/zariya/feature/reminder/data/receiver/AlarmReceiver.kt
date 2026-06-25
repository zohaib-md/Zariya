package com.project.zariya.feature.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.project.zariya.feature.reminder.data.scheduler.AlarmSchedulerImpl
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import com.project.zariya.feature.reminder.presentation.notification.ReminderNotificationBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * BroadcastReceiver that handles alarm triggers for medicine reminders.
 * When an alarm fires:
 * 1. Extracts reminder details from the intent
 * 2. Builds and shows a notification via [ReminderNotificationBuilder]
 * 3. Logs a PENDING dose in the repository
 * 4. Calculates and schedules the next alarm trigger
 *
 * Uses goAsync() to perform asynchronous work safely within the receiver's lifecycle.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationBuilder: ReminderNotificationBuilder

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val reminderId = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_REMINDER_ID)
        val medicineName = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_MEDICINE_NAME) ?: "Medicine"
        val dosage = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_DOSAGE) ?: ""
        val medicineForm = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_MEDICINE_FORM) ?: "TABLET"

        if (reminderId.isNullOrBlank()) {
            Log.e(TAG, "Received alarm with null or blank reminderId, ignoring")
            pendingResult.finish()
            return
        }

        Log.d(TAG, "Alarm triggered for reminder $reminderId ($medicineName)")

        scope.launch {
            try {
                val exactScheduledTime = System.currentTimeMillis()

                // 1. Build and show the notification
                val notification = notificationBuilder.buildReminderNotification(
                    reminderId = reminderId,
                    medicineName = medicineName,
                    dosage = dosage,
                    medicineForm = medicineForm,
                    scheduledTime = exactScheduledTime
                )
                notificationBuilder.showNotification(reminderId, notification)

                // 2. Fetch the full reminder to get medicineId and profileId for the dose log
                val reminder = reminderRepository.getReminderById(reminderId)

                // 3. Log a PENDING dose
                val doseLog = DoseLog(
                    id = UUID.randomUUID().toString(),
                    reminderId = reminderId,
                    medicineId = reminder?.medicineId ?: "",
                    medicineName = medicineName,
                    profileId = reminder?.profileId ?: "",
                    scheduledTime = exactScheduledTime,
                    actionTime = null,
                    status = DoseStatus.PENDING,
                    snoozedCount = 0
                )
                reminderRepository.logDose(doseLog)

                // 4. Reschedule the next alarm for this reminder
                if (reminder != null && reminder.isActive) {
                    alarmScheduler.scheduleReminder(reminder)
                    Log.d(TAG, "Next alarm scheduled for reminder $reminderId")
                } else {
                    Log.d(TAG, "Reminder $reminderId is null or inactive, not rescheduling")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing alarm for reminder $reminderId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
