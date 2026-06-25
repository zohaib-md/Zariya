package com.project.zariya.feature.medicine.domain.model

data class Medicine(
    val id: String,
    val profileId: String,
    val name: String,
    val genericName: String,
    val dosage: String,
    val dosageUnit: String,
    val category: MedicineCategory,
    val form: MedicineForm,
    val manufacturer: String,
    val notes: String,
    val prescriptionNotes: String,
    val doctorName: String,
    val doctorPhone: String,
    val prescriptionImageUri: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean,
    val stockCount: Int = 0,
    val isStockTracked: Boolean = false,
    val totalVolume: Int? = null,
    val volumePerDose: Int? = null,
    val stockAlertThreshold: Int = 10
) {
    val currentStockUnit: String
        get() = when (form) {
            MedicineForm.SYRUP, MedicineForm.DROPS, MedicineForm.INJECTION -> "ml"
            else -> "units" // capsules, tablets, etc.
        }

    val currentStock: Int
        get() = when (form) {
            MedicineForm.SYRUP, MedicineForm.DROPS, MedicineForm.INJECTION -> totalVolume ?: 0
            else -> stockCount
        }
}
