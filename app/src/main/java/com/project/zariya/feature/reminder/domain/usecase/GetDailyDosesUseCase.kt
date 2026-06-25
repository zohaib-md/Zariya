package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.model.ScheduleType
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

class GetDailyDosesUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(profileId: String, dateMillis: Long): Flow<List<DoseLog>> {
        val zone = ZoneId.systemDefault()
        val targetDate = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
        val startOfDay = targetDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = targetDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val activeRemindersFlow = reminderRepository.getReminders(profileId)
            .map { it.filter { reminder -> reminder.isActive } }
        val doseLogsFlow = reminderRepository.getDoseLogsForDate(profileId, startOfDay, endOfDay)

        return combine(activeRemindersFlow, doseLogsFlow) { reminders, existingLogs ->
            val dailyDoses = mutableListOf<DoseLog>()
            val now = System.currentTimeMillis()

            for (reminder in reminders) {
                // If reminder hasn't started yet, skip
                if (reminder.startDate > endOfDay) continue

                // Check if the reminder is scheduled for this specific date
                if (isScheduledForDate(reminder, targetDate)) {
                    val expectedTimes = getExpectedTimesForDate(reminder, targetDate, zone)

                    for (expectedTime in expectedTimes) {
                        // Check if an existing log matches this time
                        // We allow a small 5-minute buffer in case the alarm fired slightly off-schedule
                        val existingLog = existingLogs.find {
                            it.reminderId == reminder.id &&
                            kotlin.math.abs(it.scheduledTime - expectedTime) < 5 * 60 * 1000
                        }

                        if (existingLog != null) {
                            dailyDoses.add(existingLog)
                        } else {
                            // Determine status for virtual log
                            val status = if (expectedTime < now) DoseStatus.MISSED else DoseStatus.PENDING
                            
                            dailyDoses.add(
                                DoseLog(
                                    id = "virtual_${reminder.id}_$expectedTime",
                                    reminderId = reminder.id,
                                    medicineId = reminder.medicineId,
                                    medicineName = reminder.medicineName,
                                    profileId = reminder.profileId,
                                    scheduledTime = expectedTime,
                                    actionTime = null,
                                    status = status,
                                    snoozedCount = 0
                                )
                            )
                        }
                    }
                }
            }
            
            // Also include any existing logs that might be from inactive reminders or manual logs
            val unmatchedLogs = existingLogs.filter { log ->
                dailyDoses.none { it.id == log.id || (it.reminderId == log.reminderId && kotlin.math.abs(it.scheduledTime - log.scheduledTime) < 5 * 60 * 1000) }
            }
            
            dailyDoses.addAll(unmatchedLogs)
            dailyDoses.sortedBy { it.scheduledTime }
        }
    }

    private fun isScheduledForDate(reminder: Reminder, date: LocalDate): Boolean {
        val zone = ZoneId.systemDefault()
        return when (reminder.scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.EVERY_N_HOURS -> true
            ScheduleType.SPECIFIC_DAYS -> {
                val dayValue = date.dayOfWeek.value
                dayValue in reminder.selectedDays
            }
            ScheduleType.CYCLIC -> {
                val start = Instant.ofEpochMilli(reminder.startDate).atZone(zone).toLocalDate()
                val daysSinceStart = ChronoUnit.DAYS.between(start, date).toInt()
                if (daysSinceStart < 0) return false

                val activeDays = if (reminder.cycleDaysOn > 0) reminder.cycleDaysOn else 21
                val offDays = if (reminder.cycleDaysOff > 0) reminder.cycleDaysOff else 7
                val cycleLength = activeDays + offDays
                val positionInCycle = daysSinceStart % cycleLength
                positionInCycle < activeDays
            }
        }
    }

    private fun getExpectedTimesForDate(reminder: Reminder, date: LocalDate, zone: ZoneId): List<Long> {
        val times = mutableListOf<Long>()
        
        if (reminder.scheduleType == ScheduleType.EVERY_N_HOURS) {
            val intervalHours = if (reminder.intervalHours > 0) reminder.intervalHours else 8
            val intervalMillis = intervalHours * 60 * 60 * 1000L
            
            // Compute based on start date
            val startMillis = reminder.startDate
            val startOfDayMillis = date.atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfDayMillis = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            
            var currentTime = startMillis
            while (currentTime <= endOfDayMillis) {
                if (currentTime >= startOfDayMillis) {
                    times.add(currentTime)
                }
                currentTime += intervalMillis
            }
        } else {
            for (timeStr in reminder.scheduledTimes) {
                val localTime = parseTimeString(timeStr)
                if (localTime != null) {
                    times.add(LocalDateTime.of(date, localTime).atZone(zone).toInstant().toEpochMilli())
                }
            }
        }
        
        return times
    }
    
    private fun parseTimeString(timeStr: String): LocalTime? {
        return try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)
            LocalTime.parse(timeStr.uppercase(), formatter)
        } catch (e: Exception) {
            try {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    LocalTime.of(parts[0].trim().toInt(), parts[1].trim().substringBefore(" ").toInt())
                } else null
            } catch (e2: Exception) {
                null
            }
        }
    }
}
