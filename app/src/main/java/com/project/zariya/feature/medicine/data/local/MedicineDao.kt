package com.project.zariya.feature.medicine.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND isActive = 1 ORDER BY createdAt DESC")
    fun getAllByProfile(profileId: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: String): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND name LIKE '%' || :query || '%' AND isActive = 1")
    fun searchByName(profileId: String, query: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE profileId = :profileId AND category = :category AND isActive = 1")
    fun getByCategory(profileId: String, category: String): Flow<List<MedicineEntity>>

    @Query("SELECT name FROM medicines WHERE profileId = :profileId AND isActive = 1")
    suspend fun getActiveMedicineNames(profileId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: MedicineEntity)

    @Update
    suspend fun update(medicine: MedicineEntity)

    @Query("UPDATE medicines SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun deactivate(id: String, timestamp: Long)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM medicines WHERE profileId = :profileId AND isActive = 1")
    fun getCount(profileId: String): Flow<Int>
}
