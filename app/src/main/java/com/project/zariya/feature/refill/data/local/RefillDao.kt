package com.project.zariya.feature.refill.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RefillDao {
    @Query("SELECT * FROM refill_info WHERE profileId = :profileId ORDER BY predictedRunOutDate ASC")
    fun getByProfile(profileId: String): Flow<List<RefillInfoEntity>>

    @Query("SELECT * FROM refill_info WHERE medicineId = :medicineId")
    suspend fun getByMedicine(medicineId: String): RefillInfoEntity?

    @Query("SELECT * FROM refill_info WHERE id = :id")
    suspend fun getById(id: String): RefillInfoEntity?

    @Query("SELECT * FROM refill_info WHERE profileId = :profileId AND isLowStock = 1")
    fun getLowStockItems(profileId: String): Flow<List<RefillInfoEntity>>

    @Query("SELECT COUNT(*) FROM refill_info WHERE profileId = :profileId AND isLowStock = 1")
    fun getLowStockCount(profileId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(refillInfo: RefillInfoEntity)

    @Update
    suspend fun update(refillInfo: RefillInfoEntity)

    @Query("UPDATE refill_info SET currentStock = :newStock, isLowStock = :isLow, predictedRunOutDate = :runOutDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStock(id: String, newStock: Int, isLow: Boolean, runOutDate: Long?, updatedAt: Long)

    @Query("DELETE FROM refill_info WHERE id = :id")
    suspend fun delete(id: String)
}
