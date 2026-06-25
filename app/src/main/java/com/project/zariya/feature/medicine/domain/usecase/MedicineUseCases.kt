package com.project.zariya.feature.medicine.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import javax.inject.Inject

class UpdateMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    suspend operator fun invoke(medicine: Medicine): Result<Unit> {
        if (medicine.id.isBlank()) {
            return Result.error("Medicine ID is required for updating")
        }
        if (medicine.name.isBlank()) {
            return Result.error("Medicine name cannot be blank")
        }

        val updatedMedicine = medicine.copy(
            updatedAt = System.currentTimeMillis()
        )

        return try {
            repository.updateMedicine(updatedMedicine)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to update medicine: ${e.message}")
        }
    }
}

class DeleteMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            repository.deleteMedicine(id) // This is a soft delete
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete medicine: ${e.message}")
        }
    }
}

class GetMedicinesUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    operator fun invoke(profileId: String) = repository.getMedicines(profileId)
}
