package com.project.zariya.feature.reminder.data.repository

import com.project.zariya.feature.reminder.data.local.DoseLogDao
import com.project.zariya.feature.reminder.data.local.ReminderDao
import com.project.zariya.feature.reminder.data.mapper.ReminderMapper.toDomain
import com.project.zariya.feature.reminder.data.mapper.ReminderMapper.toEntity
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val doseLogDao: DoseLogDao
) : ReminderRepository {

    override fun getReminders(profileId: String): Flow<List<Reminder>> {
        return reminderDao.getByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRemindersByMedicine(medicineId: String): Flow<List<Reminder>> {
        return reminderDao.getByMedicine(medicineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveReminders(): List<Reminder> {
        return reminderDao.getActiveReminders().map { it.toDomain() }
    }

    override suspend fun getReminderById(id: String): Reminder? {
        return reminderDao.getById(id)?.toDomain()
    }

    override suspend fun getNextReminder(profileId: String): Reminder? {
        return reminderDao.getNextReminder(profileId, System.currentTimeMillis())?.toDomain()
    }

    override suspend fun addReminder(reminder: Reminder) {
        reminderDao.insert(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.update(reminder.toEntity())
    }

    override suspend fun updateNextTriggerTime(id: String, nextTriggerTime: Long) {
        reminderDao.updateNextTriggerTime(id, nextTriggerTime)
    }

    override suspend fun deactivateReminder(id: String) {
        reminderDao.deactivate(id)
    }

    override suspend fun deleteReminder(id: String) {
        reminderDao.delete(id)
    }

    override suspend fun logDose(doseLog: DoseLog) {
        doseLogDao.insert(doseLog.toEntity())
    }

    override fun getDoseLogsForDate(
        profileId: String,
        startOfDay: Long,
        endOfDay: Long
    ): Flow<List<DoseLog>> {
        return doseLogDao.getLogsForDate(profileId, startOfDay, endOfDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDoseLogsForReminder(reminderId: String): Flow<List<DoseLog>> {
        return doseLogDao.getLogsForReminder(reminderId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDoseLogsForMedicine(medicineId: String): Flow<List<DoseLog>> {
        return doseLogDao.getLogsForMedicine(medicineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMissedDoseCount(
        profileId: String,
        startDate: Long,
        endDate: Long
    ): Int {
        return doseLogDao.getMissedCount(profileId, startDate, endDate)
    }

    override suspend fun getTakenDoseCount(
        profileId: String,
        startDate: Long,
        endDate: Long
    ): Int {
        return doseLogDao.getTakenCount(profileId, startDate, endDate)
    }

    override suspend fun getDoseLogByReminderAndTime(
        reminderId: String,
        scheduledTime: Long
    ): DoseLog? {
        return doseLogDao.getLogByReminderAndTime(reminderId, scheduledTime)?.toDomain()
    }

    override fun getRecentDoseLogs(profileId: String, limit: Int): Flow<List<DoseLog>> {
        return doseLogDao.getRecentLogs(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPendingLogsOlderThan(timestamp: Long): List<DoseLog> {
        return doseLogDao.getPendingLogsOlderThan(timestamp).map { it.toDomain() }
    }
}
