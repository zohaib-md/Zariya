package com.project.zariya.feature.reminder.domain.model

data class DoseLog(
    val id: String,
    val reminderId: String,
    val medicineId: String,
    val medicineName: String,
    val profileId: String,
    val scheduledTime: Long,
    val actionTime: Long? = null,
    val status: DoseStatus,
    val skipReason: String? = null,
    val snoozedCount: Int = 0
)
