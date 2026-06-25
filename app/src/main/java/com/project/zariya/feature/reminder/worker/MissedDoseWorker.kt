package com.project.zariya.feature.reminder.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.project.zariya.feature.reminder.domain.usecase.DetectMissedDosesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * A WorkManager worker that periodically checks for missed doses.
 * It invokes [DetectMissedDosesUseCase] which handles the logic
 * of transitioning PENDING doses to MISSED if they exceed the threshold.
 */
@HiltWorker
class MissedDoseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val detectMissedDosesUseCase: DetectMissedDosesUseCase
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "MissedDoseWorker"
        const val WORK_NAME = "detect_missed_doses_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Running MissedDoseWorker...")
        return try {
            detectMissedDosesUseCase()
            Log.d(TAG, "MissedDoseWorker completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in MissedDoseWorker", e)
            Result.retry()
        }
    }
}
