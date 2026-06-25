package com.project.zariya.feature.reminder.presentation.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.project.zariya.MainActivity
import com.project.zariya.feature.reminder.data.receiver.DoseActionReceiver
import com.project.zariya.feature.reminder.data.receiver.SnoozeReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and manages notifications for medicine reminders.
 * Creates notification channels and constructs rich notifications with
 * Take, Skip, and Snooze action buttons.
 */
@Singleton
class ReminderNotificationBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_ID_REMINDERS = "zariya_reminders"
        const val CHANNEL_NAME_REMINDERS = "Medicine Reminders"
        const val CHANNEL_DESCRIPTION_REMINDERS = "Notifications for scheduled medicine reminders"

        const val ACTION_TAKE = "TAKE"
        const val ACTION_SKIP = "SKIP"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_ACTION = "extra_action"
        const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

        const val DEFAULT_SNOOZE_MINUTES = 10
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * Creates the high-importance notification channel for medicine reminders.
     * Safe to call multiple times — the system ignores re-creation with same ID.
     */
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_REMINDERS,
            CHANNEL_NAME_REMINDERS,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION_REMINDERS
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            enableLights(true)
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Builds a rich reminder notification with Take, Skip, and Snooze actions.
     *
     * @param reminderId Unique identifier of the reminder
     * @param medicineName Display name of the medicine
     * @param dosage Dosage string (e.g., "500mg", "2 tablets")
     * @param medicineForm Form of the medicine (e.g., "TABLET", "SYRUP")
     * @return A fully configured [Notification] ready to be shown
     */
    fun buildReminderNotification(
        reminderId: String,
        medicineName: String,
        dosage: String,
        medicineForm: String,
        scheduledTime: Long
    ): Notification {

        // Content intent — opens the app when notification is tapped
        val contentIntent = createContentIntent(reminderId)

        // Full-screen intent for high-urgency display
        val fullScreenIntent = createFullScreenIntent(reminderId)

        // Action: Take
        val takeAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "Take",
            createDoseActionIntent(reminderId, ACTION_TAKE, scheduledTime)
        ).build()

        // Action: Skip
        val skipAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_delete,
            "Skip",
            createDoseActionIntent(reminderId, ACTION_SKIP, scheduledTime)
        ).build()

        // Action: Snooze
        val snoozeAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_popup_reminder,
            "Snooze ${DEFAULT_SNOOZE_MINUTES}m",
            createSnoozeIntent(reminderId, DEFAULT_SNOOZE_MINUTES)
        ).build()

        val formEmoji = getFormEmoji(medicineForm)

        return NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$formEmoji Time for $medicineName")
            .setContentText("Take $dosage")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Take $dosage of $medicineName\nForm: ${medicineForm.lowercase().replaceFirstChar { it.uppercase() }}")
                    .setBigContentTitle("$formEmoji Time for $medicineName")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(takeAction)
            .addAction(skipAction)
            .addAction(snoozeAction)
            .setOngoing(false)
            .build()
    }

    /**
     * Shows a notification for the given reminder.
     *
     * @param reminderId Used to derive a unique notification ID
     * @param notification The notification to display
     */
    fun showNotification(reminderId: String, notification: Notification) {
        val notificationId = reminderId.hashCode()
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Dismisses the notification associated with the given reminder ID.
     *
     * @param reminderId The reminder whose notification should be dismissed
     */
    fun dismissNotification(reminderId: String) {
        val notificationId = reminderId.hashCode()
        notificationManager.cancel(notificationId)
    }

    private fun createContentIntent(reminderId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createFullScreenIntent(reminderId: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra("full_screen", true)
        }
        return PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createDoseActionIntent(
        reminderId: String,
        action: String,
        scheduledTime: Long
    ): PendingIntent {
        val intent = Intent(context, DoseActionReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_ACTION, action)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        val requestCode = (reminderId + action).hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createSnoozeIntent(reminderId: String, snoozeMinutes: Int): PendingIntent {
        val intent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        val requestCode = (reminderId + "SNOOZE").hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getFormEmoji(medicineForm: String): String {
        return when (medicineForm.uppercase()) {
            "TABLET" -> "💊"
            "CAPSULE" -> "💊"
            "SYRUP" -> "🥤"
            "INJECTION" -> "💉"
            "DROPS" -> "💧"
            "INHALER" -> "🫁"
            "CREAM" -> "🧴"
            "PATCH" -> "🩹"
            else -> "💊"
        }
    }
}
