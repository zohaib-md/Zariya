package com.project.zariya.feature.health.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.health.data.HealthConnectManager
import com.project.zariya.feature.health.domain.model.HealthMetricType
import com.project.zariya.feature.health.domain.model.HealthRecord
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetHealthDataUseCase @Inject constructor(
    private val healthConnectManager: HealthConnectManager
) {

    suspend fun getBloodPressureHistory(days: Int = 30): Result<List<HealthRecord.BloodPressureRecord>> {
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(days.toLong(), ChronoUnit.DAYS)
            val records = healthConnectManager.readBloodPressure(startTime, endTime)
            Result.Success(records.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to read blood pressure data",
                throwable = e
            )
        }
    }

    suspend fun getWeightHistory(days: Int = 90): Result<List<HealthRecord.WeightRecord>> {
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(days.toLong(), ChronoUnit.DAYS)
            val records = healthConnectManager.readWeight(startTime, endTime)
            Result.Success(records.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to read weight data",
                throwable = e
            )
        }
    }

    suspend fun getBloodGlucoseHistory(days: Int = 30): Result<List<HealthRecord.BloodGlucoseRecord>> {
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(days.toLong(), ChronoUnit.DAYS)
            val records = healthConnectManager.readBloodGlucose(startTime, endTime)
            Result.Success(records.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to read blood glucose data",
                throwable = e
            )
        }
    }

    suspend fun getHeartRateHistory(days: Int = 7): Result<List<HealthRecord.HeartRateRecord>> {
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(days.toLong(), ChronoUnit.DAYS)
            val records = healthConnectManager.readHeartRate(startTime, endTime)
            Result.Success(records.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to read heart rate data",
                throwable = e
            )
        }
    }

    suspend fun getLatestMetrics(): Result<Map<HealthMetricType, HealthRecord?>> {
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(90, ChronoUnit.DAYS)
            val allMetrics = healthConnectManager.readAllMetrics(startTime, endTime)

            val latestMap = mapOf(
                HealthMetricType.BLOOD_PRESSURE to allMetrics[HealthMetricType.BLOOD_PRESSURE]
                    ?.maxByOrNull { it.timestamp },
                HealthMetricType.WEIGHT to allMetrics[HealthMetricType.WEIGHT]
                    ?.maxByOrNull { it.timestamp },
                HealthMetricType.BLOOD_GLUCOSE to allMetrics[HealthMetricType.BLOOD_GLUCOSE]
                    ?.maxByOrNull { it.timestamp },
                HealthMetricType.HEART_RATE to allMetrics[HealthMetricType.HEART_RATE]
                    ?.maxByOrNull { it.timestamp }
            )

            Result.Success(latestMap)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to read latest health metrics",
                throwable = e
            )
        }
    }
}
