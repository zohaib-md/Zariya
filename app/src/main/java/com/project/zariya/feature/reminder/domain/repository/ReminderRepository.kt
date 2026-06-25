package com.project.zariya.feature.reminder.domain.repository

import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    fun getReminders(profileId: String): Flow<List<Reminder>>

    fun getRemindersByMedicine(medicineId: String): Flow<List<Reminder>>

    suspend fun getActiveReminders(): List<Reminder>

    suspend fun getReminderById(id: String): Reminder?

    suspend fun getNextReminder(profileId: String): Reminder?

    suspend fun addReminder(reminder: Reminder)

    suspend fun updateReminder(reminder: Reminder)

    suspend fun updateNextTriggerTime(id: String, nextTriggerTime: Long)

    suspend fun deactivateReminder(id: String)

    suspend fun deleteReminder(id: String)

    suspend fun logDose(doseLog: DoseLog)

    fun getDoseLogsForDate(profileId: String, startOfDay: Long, endOfDay: Long): Flow<List<DoseLog>>

    fun getDoseLogsForReminder(reminderId: String): Flow<List<DoseLog>>

    fun getDoseLogsForMedicine(medicineId: String): Flow<List<DoseLog>>

    suspend fun getMissedDoseCount(profileId: String, startDate: Long, endDate: Long): Int

    suspend fun getTakenDoseCount(profileId: String, startDate: Long, endDate: Long): Int

    suspend fun getDoseLogByReminderAndTime(reminderId: String, scheduledTime: Long): DoseLog?

    fun getRecentDoseLogs(profileId: String, limit: Int): Flow<List<DoseLog>>

    suspend fun getPendingLogsOlderThan(timestamp: Long): List<DoseLog>
}
