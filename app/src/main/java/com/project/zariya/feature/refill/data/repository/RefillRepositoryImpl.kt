package com.project.zariya.feature.refill.data.repository

import com.project.zariya.feature.refill.data.local.RefillDao
import com.project.zariya.feature.refill.data.mapper.RefillMapper.toDomain
import com.project.zariya.feature.refill.data.mapper.RefillMapper.toEntity
import com.project.zariya.feature.refill.domain.model.RefillInfo
import com.project.zariya.feature.refill.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefillRepositoryImpl @Inject constructor(
    private val refillDao: RefillDao
) : RefillRepository {

    override fun getRefills(profileId: String): Flow<List<RefillInfo>> {
        return refillDao.getByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRefillByMedicine(medicineId: String): RefillInfo? {
        return refillDao.getByMedicine(medicineId)?.toDomain()
    }

    override suspend fun getRefillById(id: String): RefillInfo? {
        return refillDao.getById(id)?.toDomain()
    }

    override fun getLowStockItems(profileId: String): Flow<List<RefillInfo>> {
        return refillDao.getLowStockItems(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLowStockCount(profileId: String): Flow<Int> {
        return refillDao.getLowStockCount(profileId)
    }

    override suspend fun addRefill(refillInfo: RefillInfo) {
        refillDao.insert(refillInfo.toEntity())
    }

    override suspend fun updateRefill(refillInfo: RefillInfo) {
        refillDao.update(refillInfo.toEntity())
    }

    override suspend fun updateStock(id: String, newStock: Int, isLow: Boolean, runOutDate: Long?) {
        refillDao.updateStock(id, newStock, isLow, runOutDate, System.currentTimeMillis())
    }

    override suspend fun deleteRefill(id: String) {
        refillDao.delete(id)
    }
}
