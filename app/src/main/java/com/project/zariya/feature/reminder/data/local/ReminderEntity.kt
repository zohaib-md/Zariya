package com.project.zariya.feature.reminder.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.project.zariya.feature.medicine.data.local.MedicineEntity
import com.project.zariya.feature.profile.data.local.ProfileEntity

@Entity(
    tableName = "reminders",
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
data class ReminderEntity(
    @PrimaryKey val id: String,
    val medicineId: String,
    val medicineName: String,
    val profileId: String,
    val scheduleType: String,
    val scheduledTimes: List<String>,
    val selectedDays: List<Int>,
    val intervalHours: Int,
    val cycleDaysOn: Int,
    val cycleDaysOff: Int,
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean,
    val snoozeMinutes: Int,
    val nextTriggerTime: Long,
    val dosage: String,
    val medicineForm: String
)
