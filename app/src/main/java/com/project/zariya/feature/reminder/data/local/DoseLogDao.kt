package com.project.zariya.feature.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doseLog: DoseLogEntity)

    @Query("SELECT * FROM dose_logs WHERE profileId = :profileId AND scheduledTime >= :startOfDay AND scheduledTime <= :endOfDay ORDER BY scheduledTime ASC")
    fun getLogsForDate(profileId: String, startOfDay: Long, endOfDay: Long): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE reminderId = :reminderId ORDER BY scheduledTime DESC")
    fun getLogsForReminder(reminderId: String): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId ORDER BY scheduledTime DESC")
    fun getLogsForMedicine(medicineId: String): Flow<List<DoseLogEntity>>

    @Query("SELECT COUNT(*) FROM dose_logs WHERE profileId = :profileId AND scheduledTime >= :startDate AND scheduledTime <= :endDate AND status = 'MISSED'")
    suspend fun getMissedCount(profileId: String, startDate: Long, endDate: Long): Int

    @Query("SELECT COUNT(*) FROM dose_logs WHERE profileId = :profileId AND scheduledTime >= :startDate AND scheduledTime <= :endDate AND status = 'TAKEN'")
    suspend fun getTakenCount(profileId: String, startDate: Long, endDate: Long): Int

    @Query("SELECT * FROM dose_logs WHERE reminderId = :reminderId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun getLogByReminderAndTime(reminderId: String, scheduledTime: Long): DoseLogEntity?

    @Query("SELECT * FROM dose_logs WHERE profileId = :profileId ORDER BY scheduledTime DESC LIMIT :limit")
    fun getRecentLogs(profileId: String, limit: Int): Flow<List<DoseLogEntity>>

    @Query("SELECT * FROM dose_logs WHERE status = 'PENDING' AND scheduledTime < :timestamp")
    suspend fun getPendingLogsOlderThan(timestamp: Long): List<DoseLogEntity>
}
