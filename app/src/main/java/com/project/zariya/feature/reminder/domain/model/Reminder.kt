package com.project.zariya.feature.reminder.domain.model

data class Reminder(
    val id: String,
    val medicineId: String,
    val medicineName: String,
    val profileId: String,
    val scheduleType: ScheduleType,
    val scheduledTimes: List<String>,
    val selectedDays: List<Int>,
    val intervalHours: Int = 0,
    val cycleDaysOn: Int = 0,
    val cycleDaysOff: Int = 0,
    val startDate: Long = 0L,
    val endDate: Long? = null,
    val isActive: Boolean = true,
    val snoozeMinutes: Int = 10,
    val nextTriggerTime: Long = 0L,
    val dosage: String = "",
    val medicineForm: String = "TABLET"
)
