package com.project.zariya.feature.reminder.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.project.zariya.feature.reminder.data.receiver.AlarmReceiver
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.model.ScheduleType
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AlarmScheduler] that uses [AlarmManager] to schedule exact alarms
 * for medicine reminders. Alarms are delivered to [AlarmReceiver] via PendingIntent.
 */
@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository
) : AlarmScheduler {

    companion object {
        private const val TAG = "AlarmSchedulerImpl"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_MEDICINE_FORM = "extra_medicine_form"
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        private const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleReminder(reminder: Reminder) {
        val triggerTime = calculateNextTriggerTime(reminder)
        if (triggerTime <= 0L) {
            Log.w(TAG, "Invalid trigger time for reminder ${reminder.id}, skipping schedule")
            return
        }

        val pendingIntent = createPendingIntent(reminder)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fall back to inexact alarm if exact alarm permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.w(TAG, "Exact alarm permission not granted, using inexact alarm for ${reminder.id}")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }

            Log.d(TAG, "Scheduled alarm for reminder ${reminder.id} " +
                    "(${reminder.medicineName}) at ${formatMillis(triggerTime)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm for ${reminder.id}", e)
        }
    }

    override fun cancelReminder(reminderId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled alarm for reminder $reminderId")
        }
    }

    override suspend fun rescheduleAllReminders() {
        try {
            val activeReminders = reminderRepository.getActiveReminders()
            activeReminders.forEach { reminder ->
                cancelReminder(reminder.id)
                scheduleReminder(reminder)
            }
            Log.d(TAG, "Rescheduled ${activeReminders.size} active reminders")
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling reminders", e)
        }
    }

    /**
     * Calculates the next trigger time in epoch milliseconds based on the reminder's schedule type.
     *
     * Supported schedule types:
     * - DAILY: Next occurrence of the scheduled time (today if still in future, otherwise tomorrow)
     * - EVERY_N_HOURS: Current time + interval hours
     * - SPECIFIC_DAYS: Next occurrence on a matching day of the week
     * - CYCLIC: Based on on/off day cycles from the start date
     */
    fun calculateNextTriggerTime(reminder: Reminder): Long {
        val now = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()

        return when (reminder.scheduleType) {
            ScheduleType.DAILY -> {
                calculateDailyTrigger(reminder, now, zone)
            }

            ScheduleType.EVERY_N_HOURS -> {
                val intervalHours = if (reminder.intervalHours > 0) reminder.intervalHours else 8
                val intervalMillis = intervalHours * MILLIS_PER_HOUR
                now + intervalMillis
            }

            ScheduleType.SPECIFIC_DAYS -> {
                calculateSpecificDaysTrigger(reminder, now, zone)
            }

            ScheduleType.CYCLIC -> {
                calculateCyclicTrigger(reminder, now, zone)
            }
        }
    }

    private fun calculateDailyTrigger(reminder: Reminder, now: Long, zone: ZoneId): Long {
        val scheduledTimes = reminder.scheduledTimes
        if (scheduledTimes.isEmpty()) return now + MILLIS_PER_DAY

        val today = LocalDate.now(zone)
        val nowInstant = Instant.ofEpochMilli(now)
        val nowLocalTime = LocalTime.ofInstant(nowInstant, zone)

        // Find the next scheduled time today
        for (timeStr in scheduledTimes.sorted()) {
            val scheduledTime = parseTimeString(timeStr) ?: continue
            if (scheduledTime.isAfter(nowLocalTime)) {
                return LocalDateTime.of(today, scheduledTime)
                    .atZone(zone)
                    .toInstant()
                    .toEpochMilli()
            }
        }

        // All times today have passed, schedule for the first time tomorrow
        val firstTime = scheduledTimes.sorted().firstNotNullOfOrNull { parseTimeString(it) }
            ?: return now + MILLIS_PER_DAY

        return LocalDateTime.of(today.plusDays(1), firstTime)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }

    private fun calculateSpecificDaysTrigger(reminder: Reminder, now: Long, zone: ZoneId): Long {
        val selectedDays = reminder.selectedDays
        if (selectedDays.isEmpty()) return calculateDailyTrigger(reminder, now, zone)

        val today = LocalDate.now(zone)
        val nowInstant = Instant.ofEpochMilli(now)
        val nowLocalTime = LocalTime.ofInstant(nowInstant, zone)
        val todayDayValue = today.dayOfWeek.value // Monday = 1, Sunday = 7

        val firstScheduledTime = reminder.scheduledTimes.sorted()
            .firstNotNullOfOrNull { parseTimeString(it) }
            ?: LocalTime.of(9, 0) // Default to 9 AM

        // Check if today is a scheduled day and there's still a time slot remaining
        if (todayDayValue in selectedDays) {
            for (timeStr in reminder.scheduledTimes.sorted()) {
                val scheduledTime = parseTimeString(timeStr) ?: continue
                if (scheduledTime.isAfter(nowLocalTime)) {
                    return LocalDateTime.of(today, scheduledTime)
                        .atZone(zone)
                        .toInstant()
                        .toEpochMilli()
                }
            }
        }

        // Find the next matching day
        for (daysAhead in 1..7) {
            val candidateDate = today.plusDays(daysAhead.toLong())
            val candidateDayValue = candidateDate.dayOfWeek.value
            if (candidateDayValue in selectedDays) {
                return LocalDateTime.of(candidateDate, firstScheduledTime)
                    .atZone(zone)
                    .toInstant()
                    .toEpochMilli()
            }
        }

        // Fallback: schedule for tomorrow
        return LocalDateTime.of(today.plusDays(1), firstScheduledTime)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }

    private fun calculateCyclicTrigger(reminder: Reminder, now: Long, zone: ZoneId): Long {
        val startDate = reminder.startDate
        val activeDays = if (reminder.cycleDaysOn > 0) reminder.cycleDaysOn else 21
        val offDays = if (reminder.cycleDaysOff > 0) reminder.cycleDaysOff else 7
        val cycleLength = activeDays + offDays

        val today = LocalDate.now(zone)
        val start = Instant.ofEpochMilli(startDate).atZone(zone).toLocalDate()
        val daysSinceStart = ChronoUnit.DAYS.between(start, today).toInt()

        // Handle case where start is in the future
        if (daysSinceStart < 0) {
            val firstTime = reminder.scheduledTimes.sorted()
                .firstNotNullOfOrNull { parseTimeString(it) }
                ?: LocalTime.of(9, 0)
            return LocalDateTime.of(start, firstTime)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        }

        val positionInCycle = daysSinceStart % cycleLength
        val isActiveDay = positionInCycle < activeDays

        if (isActiveDay) {
            // We're in an active phase, use daily trigger logic
            return calculateDailyTrigger(reminder, now, zone)
        } else {
            // We're in an off phase, find the start of the next active phase
            val daysUntilNextActive = cycleLength - positionInCycle
            val nextActiveDate = today.plusDays(daysUntilNextActive.toLong())
            val firstTime = reminder.scheduledTimes.sorted()
                .firstNotNullOfOrNull { parseTimeString(it) }
                ?: LocalTime.of(9, 0)
            return LocalDateTime.of(nextActiveDate, firstTime)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        }
    }

    private fun createPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_MEDICINE_NAME, reminder.medicineName)
            putExtra(EXTRA_DOSAGE, reminder.dosage)
            putExtra(EXTRA_MEDICINE_FORM, reminder.medicineForm)
        }

        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun parseTimeString(timeStr: String): LocalTime? {
        return try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)
            LocalTime.parse(timeStr.uppercase(), formatter)
        } catch (e: Exception) {
            try {
                // Fallback for HH:mm format if any exists
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    LocalTime.of(parts[0].trim().toInt(), parts[1].trim().substringBefore(" ").toInt())
                } else {
                    null
                }
            } catch (e2: Exception) {
                Log.w(TAG, "Failed to parse time string: $timeStr", e2)
                null
            }
        }
    }

    private fun formatMillis(millis: Long): String {
        val dateTime = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return dateTime.toString()
    }
}
