package com.project.zariya.feature.refill.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.project.zariya.feature.medicine.data.local.MedicineEntity
import com.project.zariya.feature.profile.data.local.ProfileEntity

@Entity(
    tableName = "refill_info",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["medicineId"])
    ]
)
data class RefillInfoEntity(
    @PrimaryKey val id: String,
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
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)
