package com.project.zariya.feature.reminder.data.mapper

import com.project.zariya.feature.reminder.data.local.DoseLogEntity
import com.project.zariya.feature.reminder.data.local.ReminderEntity
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.model.ScheduleType

object ReminderMapper {

    fun ReminderEntity.toDomain(): Reminder {
        val parsedScheduleType = try {
            ScheduleType.valueOf(scheduleType)
        } catch (_: IllegalArgumentException) {
            ScheduleType.DAILY
        }

        return Reminder(
            id = id,
            medicineId = medicineId,
            medicineName = medicineName,
            profileId = profileId,
            scheduleType = parsedScheduleType,
            scheduledTimes = scheduledTimes,
            selectedDays = selectedDays,
            intervalHours = intervalHours,
            cycleDaysOn = cycleDaysOn,
            cycleDaysOff = cycleDaysOff,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            snoozeMinutes = snoozeMinutes,
            nextTriggerTime = nextTriggerTime,
            dosage = dosage,
            medicineForm = medicineForm
        )
    }

    fun Reminder.toEntity(): ReminderEntity {
        return ReminderEntity(
            id = id,
            medicineId = medicineId,
            medicineName = medicineName,
            profileId = profileId,
            scheduleType = scheduleType.name,
            scheduledTimes = scheduledTimes,
            selectedDays = selectedDays,
            intervalHours = intervalHours,
            cycleDaysOn = cycleDaysOn,
            cycleDaysOff = cycleDaysOff,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
            snoozeMinutes = snoozeMinutes,
            nextTriggerTime = nextTriggerTime,
            dosage = dosage,
            medicineForm = medicineForm
        )
    }

    fun DoseLogEntity.toDomain(): DoseLog {
        val parsedStatus = try {
            DoseStatus.valueOf(status)
        } catch (_: IllegalArgumentException) {
            DoseStatus.PENDING
        }

        return DoseLog(
            id = id,
            reminderId = reminderId,
            medicineId = medicineId,
            medicineName = medicineName,
            profileId = profileId,
            scheduledTime = scheduledTime,
            actionTime = actionTime,
            status = parsedStatus,
            skipReason = skipReason,
            snoozedCount = snoozedCount
        )
    }

    fun DoseLog.toEntity(): DoseLogEntity {
        return DoseLogEntity(
            id = id,
            reminderId = reminderId,
            medicineId = medicineId,
            medicineName = medicineName,
            profileId = profileId,
            scheduledTime = scheduledTime,
            actionTime = actionTime,
            status = status.name,
            skipReason = skipReason,
            snoozedCount = snoozedCount
        )
    }
}
