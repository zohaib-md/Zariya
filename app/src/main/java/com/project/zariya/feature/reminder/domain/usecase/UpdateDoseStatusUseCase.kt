package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import java.util.UUID
import javax.inject.Inject

class UpdateDoseStatusUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val medicineRepository: MedicineRepository
) {
    suspend operator fun invoke(doseLog: DoseLog, newStatus: DoseStatus) {
        val updatedId = if (doseLog.id.startsWith("virtual_")) {
            UUID.randomUUID().toString()
        } else {
            doseLog.id
        }

        val updatedDoseLog = doseLog.copy(
            id = updatedId,
            status = newStatus,
            actionTime = if (newStatus == DoseStatus.PENDING) null else System.currentTimeMillis()
        )

        reminderRepository.logDose(updatedDoseLog)

        // Stock Deduction Logic
        if (newStatus == DoseStatus.TAKEN) {
            val medicine = medicineRepository.getMedicineById(doseLog.medicineId)
            if (medicine != null && medicine.isStockTracked) {
                val isLiquid = medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.SYRUP ||
                               medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.DROPS ||
                               medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.INJECTION

                if (isLiquid) {
                    val fallbackVolume = when (medicine.form) {
                        com.project.zariya.feature.medicine.domain.model.MedicineForm.SYRUP -> 5 // 5ml default
                        com.project.zariya.feature.medicine.domain.model.MedicineForm.DROPS -> 1 // 1ml default
                        else -> 1 // Injections etc.
                    }
                    val unitsToDeduct = medicine.volumePerDose ?: fallbackVolume
                    val updatedVolume = ((medicine.totalVolume ?: 0) - unitsToDeduct).coerceAtLeast(0)
                    medicineRepository.updateMedicine(medicine.copy(totalVolume = updatedVolume))
                } else {
                    val unitsToDeduct = 1 // Default to 1 unit (tablet/capsule) per dose
                    val updatedStock = (medicine.stockCount - unitsToDeduct).coerceAtLeast(0)
                    medicineRepository.updateMedicine(medicine.copy(stockCount = updatedStock))
                }
            }
        }
    }
}
