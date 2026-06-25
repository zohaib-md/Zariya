package com.project.zariya.feature.refill.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.refill.domain.model.RefillInfo
import com.project.zariya.feature.refill.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class DecrementStockUseCase @Inject constructor(
    private val repository: RefillRepository
) {
    suspend operator fun invoke(refillId: String): Result<RefillInfo> {
        return try {
            val refill = repository.getRefillById(refillId)
                ?: return Result.error("Refill tracking not found")

            if (refill.currentStock <= 0) {
                return Result.error("Stock is already at 0")
            }

            val newStock = refill.currentStock - 1
            val isLow = newStock <= refill.refillThreshold
            val runOutDate = if (refill.dosesPerDay > 0) {
                System.currentTimeMillis() + (newStock.toLong() / refill.dosesPerDay) * 24 * 60 * 60 * 1000
            } else null

            repository.updateStock(refillId, newStock, isLow, runOutDate)

            val updated = refill.copy(
                currentStock = newStock,
                isLowStock = isLow,
                predictedRunOutDate = runOutDate
            )
            Result.success(updated)
        } catch (e: Exception) {
            Result.error("Failed to decrement stock: ${e.message}")
        }
    }
}

class PredictRefillDateUseCase @Inject constructor() {
    operator fun invoke(currentStock: Int, dosesPerDay: Int): Long? {
        if (dosesPerDay <= 0 || currentStock <= 0) return null
        val daysRemaining = currentStock / dosesPerDay
        return System.currentTimeMillis() + daysRemaining.toLong() * 24 * 60 * 60 * 1000
    }
}

class CheckLowStockUseCase @Inject constructor(
    private val repository: RefillRepository
) {
    fun getLowStockItems(profileId: String): Flow<List<RefillInfo>> {
        return repository.getLowStockItems(profileId)
    }

    fun getLowStockCount(profileId: String): Flow<Int> {
        return repository.getLowStockCount(profileId)
    }
}

class AddRefillInfoUseCase @Inject constructor(
    private val repository: RefillRepository,
    private val predictRefillDateUseCase: PredictRefillDateUseCase
) {
    suspend operator fun invoke(refillInfo: RefillInfo): Result<Unit> {
        if (refillInfo.totalStock <= 0) {
            return Result.error("Total stock must be greater than 0")
        }
        if (refillInfo.dosesPerDay <= 0) {
            return Result.error("Doses per day must be greater than 0")
        }

        val id = refillInfo.id.ifEmpty { UUID.randomUUID().toString() }
        val currentTime = System.currentTimeMillis()
        val runOutDate = predictRefillDateUseCase(refillInfo.currentStock, refillInfo.dosesPerDay)
        val isLow = refillInfo.currentStock <= refillInfo.refillThreshold

        val newRefill = refillInfo.copy(
            id = id,
            predictedRunOutDate = runOutDate,
            isLowStock = isLow,
            createdAt = if (refillInfo.createdAt == 0L) currentTime else refillInfo.createdAt,
            updatedAt = currentTime
        )

        return try {
            repository.addRefill(newRefill)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to add refill info: ${e.message}")
        }
    }
}

class GetRefillsUseCase @Inject constructor(
    private val repository: RefillRepository
) {
    operator fun invoke(profileId: String): Flow<List<RefillInfo>> {
        return repository.getRefills(profileId)
    }
}
