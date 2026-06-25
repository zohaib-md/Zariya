package com.project.zariya.feature.reminder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.presentation.notification.ReminderNotificationBuilder
import com.project.zariya.feature.reminder.domain.usecase.UpdateDoseStatusUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles "Take" and "Skip" actions from reminder notifications.
 * When triggered:
 * 1. Extracts the action type (TAKE or SKIP) and reminder details
 * 2. Finds the pending dose log for this reminder and scheduled time
 * 3. Creates an updated dose log with the appropriate [DoseStatus] and action time
 * 4. Dismisses the notification
 *
 * Uses goAsync() to safely perform database operations within the receiver's lifecycle.
 */
@AndroidEntryPoint
class DoseActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DoseActionReceiver"
    }

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var notificationBuilder: ReminderNotificationBuilder

    @Inject
    lateinit var updateDoseStatusUseCase: UpdateDoseStatusUseCase

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val reminderId = intent.getStringExtra(ReminderNotificationBuilder.EXTRA_REMINDER_ID)
        val action = intent.getStringExtra(ReminderNotificationBuilder.EXTRA_ACTION)
        val scheduledTime = intent.getLongExtra(
            ReminderNotificationBuilder.EXTRA_SCHEDULED_TIME,
            System.currentTimeMillis()
        )

        if (reminderId.isNullOrBlank() || action.isNullOrBlank()) {
            Log.e(TAG, "Received dose action with null reminderId=$reminderId or action=$action, ignoring")
            pendingResult.finish()
            return
        }

        Log.d(TAG, "Dose action received: $action for reminder $reminderId")

        scope.launch {
            try {
                val doseStatus = when (action) {
                    ReminderNotificationBuilder.ACTION_TAKE -> DoseStatus.TAKEN
                    ReminderNotificationBuilder.ACTION_SKIP -> DoseStatus.SKIPPED
                    else -> {
                        Log.w(TAG, "Unknown action: $action, defaulting to SKIPPED")
                        DoseStatus.SKIPPED
                    }
                }

                val actionTime = System.currentTimeMillis()

                // Find the pending dose log for this reminder and update it
                val existingDoseLog = reminderRepository.getDoseLogByReminderAndTime(
                    reminderId = reminderId,
                    scheduledTime = scheduledTime
                )

                if (existingDoseLog != null) {
                    // Update dose status and deduct stock if taken
                    updateDoseStatusUseCase(existingDoseLog, doseStatus)
                    Log.d(TAG, "Dose status updated to $doseStatus for reminder $reminderId")
                } else {
                    // Fallback: create a new dose log if the pending one wasn't found
                    val reminder = reminderRepository.getReminderById(reminderId)
                    val newDoseLog = com.project.zariya.feature.reminder.domain.model.DoseLog(
                        id = java.util.UUID.randomUUID().toString(),
                        reminderId = reminderId,
                        medicineId = reminder?.medicineId ?: "",
                        medicineName = reminder?.medicineName ?: "",
                        profileId = reminder?.profileId ?: "",
                        scheduledTime = scheduledTime,
                        actionTime = null, // Will be set by usecase
                        status = DoseStatus.PENDING,
                        snoozedCount = 0
                    )
                    updateDoseStatusUseCase(newDoseLog, doseStatus)
                    Log.d(TAG, "Fallback dose created and updated to $doseStatus for reminder $reminderId")
                }

                // Dismiss the notification
                notificationBuilder.dismissNotification(reminderId)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing dose action for reminder $reminderId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
