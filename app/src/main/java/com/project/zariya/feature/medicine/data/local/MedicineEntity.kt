package com.project.zariya.feature.medicine.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.project.zariya.feature.profile.data.local.ProfileEntity

@Entity(
    tableName = "medicines",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MedicineEntity(
    @PrimaryKey val id: String,
    val profileId: String,
    val name: String,
    val genericName: String,
    val dosage: String,
    val dosageUnit: String,
    val category: String,
    val form: String,
    val manufacturer: String,
    val notes: String,
    val prescriptionNotes: String,
    val doctorName: String,
    val doctorPhone: String,
    val prescriptionImageUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean,
    val stockCount: Int = 0,
    val isStockTracked: Boolean = false,
    val totalVolume: Int? = null,
    val volumePerDose: Int? = null,
    val stockAlertThreshold: Int = 10
)
