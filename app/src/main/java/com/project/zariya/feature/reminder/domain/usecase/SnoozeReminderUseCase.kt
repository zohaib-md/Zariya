package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class SnoozeReminderUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {

    operator fun invoke(reminderId: String, snoozeMinutes: Int = 10): Result<Unit> {
        if (reminderId.isBlank()) {
            return Result.error("Reminder ID is required")
        }

        if (snoozeMinutes <= 0) {
            return Result.error("Snooze duration must be greater than 0 minutes")
        }

        return try {
            val snoozeTimeMillis = System.currentTimeMillis() + (snoozeMinutes * 60L * 1000L)
            val snoozedReminder = Reminder(
                id = reminderId,
                medicineId = "",
                medicineName = "",
                profileId = "",
                scheduleType = com.project.zariya.feature.reminder.domain.model.ScheduleType.DAILY,
                scheduledTimes = emptyList(),
                selectedDays = emptyList(),
                nextTriggerTime = snoozeTimeMillis,
                snoozeMinutes = snoozeMinutes
            )
            alarmScheduler.scheduleReminder(snoozedReminder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to snooze reminder: ${e.message}")
        }
    }
}
