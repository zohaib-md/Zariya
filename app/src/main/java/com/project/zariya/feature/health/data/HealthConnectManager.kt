package com.project.zariya.feature.health.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.project.zariya.feature.health.domain.model.HealthMetricType
import com.project.zariya.feature.health.domain.model.HealthRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val healthConnectClient: HealthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }

    fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    suspend fun hasPermissions(permissions: Set<String>): Boolean {
        if (!isAvailable()) return false
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return permissions.all { it in granted }
    }

    fun getRequiredPermissions(): Set<String> {
        return setOf(
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(BloodGlucoseRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class)
        )
    }

    suspend fun readBloodPressure(
        startTime: Instant,
        endTime: Instant
    ): List<HealthRecord.BloodPressureRecord> {
        val request = ReadRecordsRequest(
            recordType = BloodPressureRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records.map { record ->
            HealthRecord.BloodPressureRecord(
                id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                systolic = record.systolic.inMillimetersOfMercury,
                diastolic = record.diastolic.inMillimetersOfMercury,
                timestamp = record.time.toEpochMilli()
            )
        }
    }

    suspend fun readWeight(
        startTime: Instant,
        endTime: Instant
    ): List<HealthRecord.WeightRecord> {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records.map { record ->
            HealthRecord.WeightRecord(
                id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                weight = record.weight.inKilograms,
                unit = "kg",
                timestamp = record.time.toEpochMilli()
            )
        }
    }

    suspend fun readBloodGlucose(
        startTime: Instant,
        endTime: Instant
    ): List<HealthRecord.BloodGlucoseRecord> {
        val request = ReadRecordsRequest(
            recordType = BloodGlucoseRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records.map { record ->
            HealthRecord.BloodGlucoseRecord(
                id = record.metadata.id.ifEmpty { UUID.randomUUID().toString() },
                level = record.level.inMilligramsPerDeciliter,
                mealType = mapMealType(record.relationToMeal),
                timestamp = record.time.toEpochMilli()
            )
        }
    }

    suspend fun readHeartRate(
        startTime: Instant,
        endTime: Instant
    ): List<HealthRecord.HeartRateRecord> {
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records.flatMap { record ->
            record.samples.map { sample ->
                HealthRecord.HeartRateRecord(
                    id = "${record.metadata.id}_${sample.time.toEpochMilli()}".ifEmpty {
                        UUID.randomUUID().toString()
                    },
                    bpm = sample.beatsPerMinute.toInt(),
                    timestamp = sample.time.toEpochMilli()
                )
            }
        }
    }

    suspend fun readAllMetrics(
        startTime: Instant,
        endTime: Instant
    ): Map<HealthMetricType, List<HealthRecord>> {
        return mapOf(
            HealthMetricType.BLOOD_PRESSURE to readBloodPressure(startTime, endTime),
            HealthMetricType.WEIGHT to readWeight(startTime, endTime),
            HealthMetricType.BLOOD_GLUCOSE to readBloodGlucose(startTime, endTime),
            HealthMetricType.HEART_RATE to readHeartRate(startTime, endTime)
        )
    }

    private fun mapMealType(relationToMeal: Int): String {
        return when (relationToMeal) {
            BloodGlucoseRecord.RELATION_TO_MEAL_BEFORE_MEAL -> "Before Meal"
            BloodGlucoseRecord.RELATION_TO_MEAL_AFTER_MEAL -> "After Meal"
            BloodGlucoseRecord.RELATION_TO_MEAL_FASTING -> "Fasting"
            BloodGlucoseRecord.RELATION_TO_MEAL_GENERAL -> "General"
            else -> "Unknown"
        }
    }
}
