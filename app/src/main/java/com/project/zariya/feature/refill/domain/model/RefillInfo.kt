package com.project.zariya.feature.refill.domain.model

data class RefillInfo(
    val id: String,
    val medicineId: String,
    val medicineName: String,
    val profileId: String,
    val currentStock: Int,
    val totalStock: Int,
    val dosesPerDay: Int,
    val refillThreshold: Int,
    val lastRefillDate: Long,
    val predictedRunOutDate: Long?,
    val isLowStock: Boolean,
    val notes: String = "",
    val createdAt: Long,
    val updatedAt: Long
)
