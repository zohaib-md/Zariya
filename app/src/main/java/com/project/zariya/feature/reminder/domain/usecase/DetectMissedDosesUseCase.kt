package com.project.zariya.feature.reminder.domain.usecase

import android.util.Log
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import javax.inject.Inject

/**
 * Use case to detect doses that have been PENDING for too long and mark them as MISSED.
 * This acts as a fallback for cases where the user swipes away the notification
 * or ignores it completely.
 */
class DetectMissedDosesUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    companion object {
        private const val TAG = "DetectMissedDosesUseCase"
        // 4 hours past scheduled time without action is considered missed
        private const val MISSED_THRESHOLD_MILLIS = 4 * 60 * 60 * 1000L
    }

    suspend operator fun invoke() {
        try {
            val thresholdTime = System.currentTimeMillis() - MISSED_THRESHOLD_MILLIS
            val pendingDoses = repository.getPendingLogsOlderThan(thresholdTime)

            if (pendingDoses.isEmpty()) {
                Log.d(TAG, "No missed doses detected.")
                return
            }

            Log.d(TAG, "Detected ${pendingDoses.size} missed doses. Updating status...")

            for (doseLog in pendingDoses) {
                val missedLog = doseLog.copy(
                    status = DoseStatus.MISSED,
                    actionTime = System.currentTimeMillis(),
                    skipReason = "Automatically marked missed due to timeout"
                )
                repository.logDose(missedLog)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting missed doses", e)
        }
    }
}
