package com.project.zariya.feature.medicine.domain.repository

import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    fun getMedicines(profileId: String): Flow<List<Medicine>>
    suspend fun getMedicineById(id: String): Medicine?
    fun searchMedicines(profileId: String, query: String): Flow<List<Medicine>>
    fun getMedicinesByCategory(profileId: String, category: MedicineCategory): Flow<List<Medicine>>
    suspend fun getActiveMedicineNames(profileId: String): List<String>
    suspend fun addMedicine(medicine: Medicine)
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun deleteMedicine(id: String)
    fun getMedicineCount(profileId: String): Flow<Int>
}
