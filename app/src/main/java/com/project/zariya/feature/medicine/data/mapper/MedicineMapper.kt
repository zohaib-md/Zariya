package com.project.zariya.feature.medicine.data.mapper

import com.project.zariya.feature.medicine.data.local.MedicineEntity
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import com.project.zariya.feature.medicine.domain.model.MedicineForm

object MedicineMapper {
    fun MedicineEntity.toDomain(): Medicine {
        return Medicine(
            id = id,
            profileId = profileId,
            name = name,
            genericName = genericName,
            dosage = dosage,
            dosageUnit = dosageUnit,
            category = try {
                MedicineCategory.valueOf(category)
            } catch (e: Exception) {
                MedicineCategory.OTHER
            },
            form = try {
                MedicineForm.valueOf(form)
            } catch (e: Exception) {
                MedicineForm.OTHER
            },
            manufacturer = manufacturer,
            notes = notes,
            prescriptionNotes = prescriptionNotes,
            doctorName = doctorName,
            doctorPhone = doctorPhone,
            prescriptionImageUri = prescriptionImageUri,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = isActive,
            stockCount = stockCount,
            isStockTracked = isStockTracked,
            totalVolume = totalVolume,
            volumePerDose = volumePerDose,
            stockAlertThreshold = stockAlertThreshold
        )
    }

    fun Medicine.toEntity(): MedicineEntity {
        return MedicineEntity(
            id = id,
            profileId = profileId,
            name = name,
            genericName = genericName,
            dosage = dosage,
            dosageUnit = dosageUnit,
            category = category.name,
            form = form.name,
            manufacturer = manufacturer,
            notes = notes,
            prescriptionNotes = prescriptionNotes,
            doctorName = doctorName,
            doctorPhone = doctorPhone,
            prescriptionImageUri = prescriptionImageUri,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = isActive,
            stockCount = stockCount,
            isStockTracked = isStockTracked,
            totalVolume = totalVolume,
            volumePerDose = volumePerDose,
            stockAlertThreshold = stockAlertThreshold
        )
    }
}
