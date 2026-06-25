package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import java.util.UUID
import javax.inject.Inject

class LogDoseUseCase @Inject constructor(
    private val repository: ReminderRepository
) {

    suspend operator fun invoke(
        reminderId: String,
        status: DoseStatus,
        scheduledTime: Long,
        skipReason: String? = null
    ): Result<Unit> {
        if (reminderId.isBlank()) {
            return Result.error("Reminder ID is required")
        }

        val reminder = repository.getReminderById(reminderId)
            ?: return Result.error("Reminder not found")

        val existingLog = repository.getDoseLogByReminderAndTime(reminderId, scheduledTime)

        val doseLog = DoseLog(
            id = existingLog?.id ?: UUID.randomUUID().toString(),
            reminderId = reminderId,
            medicineId = reminder.medicineId,
            medicineName = reminder.medicineName,
            profileId = reminder.profileId,
            scheduledTime = scheduledTime,
            actionTime = System.currentTimeMillis(),
            status = status,
            skipReason = skipReason,
            snoozedCount = if (status == DoseStatus.SNOOZED) {
                (existingLog?.snoozedCount ?: 0) + 1
            } else {
                existingLog?.snoozedCount ?: 0
            }
        )

        return try {
            repository.logDose(doseLog)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to log dose: ${e.message}")
        }
    }
}
