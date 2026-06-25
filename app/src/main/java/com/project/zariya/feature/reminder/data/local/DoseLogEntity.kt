package com.project.zariya.feature.reminder.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DoseLogEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(index = true) val reminderId: String,
    val medicineId: String,
    val medicineName: String,
    val profileId: String,
    val scheduledTime: Long,
    val actionTime: Long?,
    val status: String,
    val skipReason: String?,
    val snoozedCount: Int
)
