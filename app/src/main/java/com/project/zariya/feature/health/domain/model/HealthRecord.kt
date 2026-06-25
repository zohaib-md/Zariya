package com.project.zariya.feature.health.domain.model

sealed class HealthRecord {

    abstract val id: String
    abstract val timestamp: Long

    data class BloodPressureRecord(
        override val id: String,
        val systolic: Double,
        val diastolic: Double,
        override val timestamp: Long
    ) : HealthRecord()

    data class WeightRecord(
        override val id: String,
        val weight: Double,
        val unit: String,
        override val timestamp: Long
    ) : HealthRecord()

    data class BloodGlucoseRecord(
        override val id: String,
        val level: Double,
        val mealType: String,
        override val timestamp: Long
    ) : HealthRecord()

    data class HeartRateRecord(
        override val id: String,
        val bpm: Int,
        override val timestamp: Long
    ) : HealthRecord()
}

enum class HealthMetricType(
    val displayName: String,
    val unit: String
) {
    BLOOD_PRESSURE(displayName = "Blood Pressure", unit = "mmHg"),
    WEIGHT(displayName = "Weight", unit = "kg"),
    BLOOD_GLUCOSE(displayName = "Blood Glucose", unit = "mg/dL"),
    HEART_RATE(displayName = "Heart Rate", unit = "bpm")
}
