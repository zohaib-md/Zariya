package com.project.zariya.feature.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE profileId = :profileId")
    fun getByProfile(profileId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE medicineId = :medicineId")
    fun getByMedicine(medicineId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isActive = 1")
    suspend fun getActiveReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?

    @Query(
        "SELECT * FROM reminders WHERE profileId = :profileId AND isActive = 1 AND nextTriggerTime > :currentTime ORDER BY nextTriggerTime ASC LIMIT 1"
    )
    suspend fun getNextReminder(profileId: String, currentTime: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("UPDATE reminders SET nextTriggerTime = :nextTriggerTime WHERE id = :id")
    suspend fun updateNextTriggerTime(id: String, nextTriggerTime: Long)

    @Query("UPDATE reminders SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: String)
}
