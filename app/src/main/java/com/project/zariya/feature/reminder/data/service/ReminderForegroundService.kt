package com.project.zariya.feature.reminder.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.project.zariya.MainActivity
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for reliable alarm rescheduling after device reboot or app update.
 * Runs with a persistent low-importance notification to keep the process alive while
 * rescheduling all active reminders. Stops itself once rescheduling is complete.
 *
 * Uses START_STICKY to ensure the service is restarted by the system if killed.
 */
@AndroidEntryPoint
class ReminderForegroundService : Service() {

    companion object {
        private const val TAG = "ReminderForegroundSvc"
        const val CHANNEL_ID_SERVICE = "zariya_service"
        const val CHANNEL_NAME_SERVICE = "Zariya Background Service"
        const val CHANNEL_DESCRIPTION_SERVICE = "Keeps reminder alarms active and reliable"
        const val NOTIFICATION_ID_SERVICE = 9001

        /**
         * Creates an intent to start the foreground service.
         */
        fun createStartIntent(context: Context): Intent {
            return Intent(context, ReminderForegroundService::class.java)
        }
    }

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createServiceNotificationChannel()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        val notification = buildServiceNotification()
        startForeground(NOTIFICATION_ID_SERVICE, notification)

        serviceScope.launch {
            try {
                alarmScheduler.rescheduleAllReminders()
                Log.d(TAG, "All reminders rescheduled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling reminders in foreground service", e)
            } finally {
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    private fun createServiceNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_SERVICE,
            CHANNEL_NAME_SERVICE,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = CHANNEL_DESCRIPTION_SERVICE
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildServiceNotification(): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).let { intent ->
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID_SERVICE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Zariya Reminder Service")
            .setContentText("Keeping your medicine reminders active")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(contentIntent)
            .build()
    }
}
