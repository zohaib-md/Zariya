package com.project.zariya.feature.refill.data.mapper

import com.project.zariya.feature.refill.data.local.RefillInfoEntity
import com.project.zariya.feature.refill.domain.model.RefillInfo

object RefillMapper {
    fun RefillInfoEntity.toDomain(): RefillInfo {
        return RefillInfo(
            id = id,
            medicineId = medicineId,
            medicineName = medicineName,
            profileId = profileId,
            currentStock = currentStock,
            totalStock = totalStock,
            dosesPerDay = dosesPerDay,
            refillThreshold = refillThreshold,
            lastRefillDate = lastRefillDate,
            predictedRunOutDate = predictedRunOutDate,
            isLowStock = isLowStock,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun RefillInfo.toEntity(): RefillInfoEntity {
        return RefillInfoEntity(
            id = id,
            medicineId = medicineId,
            medicineName = medicineName,
            profileId = profileId,
            currentStock = currentStock,
            totalStock = totalStock,
            dosesPerDay = dosesPerDay,
            refillThreshold = refillThreshold,
            lastRefillDate = lastRefillDate,
            predictedRunOutDate = predictedRunOutDate,
            isLowStock = isLowStock,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
