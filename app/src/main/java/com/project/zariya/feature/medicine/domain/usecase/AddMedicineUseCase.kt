package com.project.zariya.feature.medicine.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import java.util.UUID
import javax.inject.Inject

class AddMedicineUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    suspend operator fun invoke(medicine: Medicine): Result<Unit> {
        if (medicine.name.isBlank()) {
            return Result.error("Medicine name cannot be blank")
        }
        if (medicine.dosage.isBlank()) {
            return Result.error("Dosage cannot be blank")
        }

        val id = medicine.id.ifEmpty { UUID.randomUUID().toString() }
        val currentTime = System.currentTimeMillis()
        
        val newMedicine = medicine.copy(
            id = id,
            createdAt = if (medicine.createdAt == 0L) currentTime else medicine.createdAt,
            updatedAt = currentTime,
            isActive = true
        )

        return try {
            repository.addMedicine(newMedicine)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to add medicine: ${e.message}")
        }
    }
}
