package com.project.zariya.feature.refill.domain.repository

import com.project.zariya.feature.refill.domain.model.RefillInfo
import kotlinx.coroutines.flow.Flow

interface RefillRepository {
    fun getRefills(profileId: String): Flow<List<RefillInfo>>
    suspend fun getRefillByMedicine(medicineId: String): RefillInfo?
    suspend fun getRefillById(id: String): RefillInfo?
    fun getLowStockItems(profileId: String): Flow<List<RefillInfo>>
    fun getLowStockCount(profileId: String): Flow<Int>
    suspend fun addRefill(refillInfo: RefillInfo)
    suspend fun updateRefill(refillInfo: RefillInfo)
    suspend fun updateStock(id: String, newStock: Int, isLow: Boolean, runOutDate: Long?)
    suspend fun deleteRefill(id: String)
}
