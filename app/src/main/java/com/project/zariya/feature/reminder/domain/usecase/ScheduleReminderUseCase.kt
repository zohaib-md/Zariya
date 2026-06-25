package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.model.ScheduleType
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class ScheduleReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {

    suspend operator fun invoke(reminder: Reminder): Result<Unit> {
        if (reminder.scheduledTimes.isEmpty()) {
            return Result.error("At least one scheduled time is required")
        }

        if (reminder.medicineId.isBlank()) {
            return Result.error("Medicine must be selected")
        }

        if (reminder.profileId.isBlank()) {
            return Result.error("Profile ID is required")
        }

        if (reminder.scheduleType == ScheduleType.SPECIFIC_DAYS && reminder.selectedDays.isEmpty()) {
            return Result.error("At least one day must be selected for specific days schedule")
        }

        if (reminder.scheduleType == ScheduleType.EVERY_N_HOURS && reminder.intervalHours <= 0) {
            return Result.error("Interval hours must be greater than 0")
        }

        val id = reminder.id.ifEmpty { UUID.randomUUID().toString() }
        val currentTime = System.currentTimeMillis()
        val nextTriggerTime = calculateNextTriggerTime(reminder)

        val scheduledReminder = reminder.copy(
            id = id,
            startDate = if (reminder.startDate == 0L) currentTime else reminder.startDate,
            nextTriggerTime = nextTriggerTime,
            isActive = reminder.isActive
        )

        return try {
            repository.addReminder(scheduledReminder)
            alarmScheduler.scheduleReminder(scheduledReminder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to schedule reminder: ${e.message}")
        }
    }

    private fun calculateNextTriggerTime(reminder: Reminder): Long {
        val now = System.currentTimeMillis()
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()

        return when (reminder.scheduleType) {
            ScheduleType.DAILY -> {
                val futureTimes = reminder.scheduledTimes.mapNotNull { timeStr ->
                    runCatching {
                        val localTime = LocalTime.parse(timeStr.uppercase(), timeFormatter)
                        val dateTime = today.atTime(localTime).atZone(zone).toInstant().toEpochMilli()
                        if (dateTime > now) dateTime
                        else today.plusDays(1).atTime(localTime).atZone(zone).toInstant().toEpochMilli()
                    }.getOrNull()
                }
                futureTimes.minOrNull() ?: now
            }

            ScheduleType.EVERY_N_HOURS -> {
                val startTime = reminder.scheduledTimes.firstOrNull()?.let { timeStr ->
                    runCatching {
                        val localTime = LocalTime.parse(timeStr.uppercase(), timeFormatter)
                        today.atTime(localTime).atZone(zone).toInstant().toEpochMilli()
                    }.getOrNull()
                } ?: now

                if (startTime > now) {
                    startTime
                } else {
                    val intervalMillis = reminder.intervalHours * 60L * 60L * 1000L
                    val elapsed = now - startTime
                    val nextInterval = ((elapsed / intervalMillis) + 1) * intervalMillis
                    startTime + nextInterval
                }
            }

            ScheduleType.SPECIFIC_DAYS -> {
                val currentDayOfWeek = today.dayOfWeek.value // 1=Mon, 7=Sun
                val sortedDays = reminder.selectedDays.sorted()

                val futureTimes = mutableListOf<Long>()
                for (day in sortedDays) {
                    val daysAhead = if (day >= currentDayOfWeek) {
                        day - currentDayOfWeek
                    } else {
                        7 - currentDayOfWeek + day
                    }
                    val targetDate = today.plusDays(daysAhead.toLong())

                    for (timeStr in reminder.scheduledTimes) {
                        runCatching {
                            val localTime = LocalTime.parse(timeStr.uppercase(), timeFormatter)
                            val dateTime = targetDate.atTime(localTime).atZone(zone).toInstant().toEpochMilli()
                            if (dateTime > now) {
                                futureTimes.add(dateTime)
                            } else if (day == currentDayOfWeek) {
                                // Same day but time passed — schedule for next week
                                val nextWeekDate = targetDate.plusDays(7)
                                futureTimes.add(
                                    nextWeekDate.atTime(localTime).atZone(zone).toInstant().toEpochMilli()
                                )
                            }
                        }
                    }
                }
                futureTimes.minOrNull() ?: now
            }

            ScheduleType.CYCLIC -> {
                val startTime = reminder.scheduledTimes.firstOrNull()?.let { timeStr ->
                    runCatching {
                        val localTime = LocalTime.parse(timeStr.uppercase(), timeFormatter)
                        today.atTime(localTime).atZone(zone).toInstant().toEpochMilli()
                    }.getOrNull()
                } ?: now

                if (startTime > now) startTime else startTime + 24L * 60L * 60L * 1000L
            }
        }
    }
}
