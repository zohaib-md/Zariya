package com.project.zariya.feature.for_her.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ForHerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycleLog(cycleLog: CycleLogEntity)

    @Query("SELECT * FROM cycle_logs WHERE profileId = :profileId ORDER BY startDateMillis DESC")
    fun getCycleLogs(profileId: String): Flow<List<CycleLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellbeingLog(log: WellbeingLogEntity)

    @Query("SELECT * FROM wellbeing_logs WHERE profileId = :profileId AND dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getWellbeingLogs(profileId: String, startMillis: Long, endMillis: Long): Flow<List<WellbeingLogEntity>>
}
