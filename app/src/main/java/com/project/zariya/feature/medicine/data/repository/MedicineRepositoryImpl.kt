package com.project.zariya.feature.medicine.data.repository

import com.project.zariya.feature.medicine.data.local.MedicineDao
import com.project.zariya.feature.medicine.data.mapper.MedicineMapper.toDomain
import com.project.zariya.feature.medicine.data.mapper.MedicineMapper.toEntity
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepositoryImpl @Inject constructor(
    private val medicineDao: MedicineDao
) : MedicineRepository {

    override fun getMedicines(profileId: String): Flow<List<Medicine>> {
        return medicineDao.getAllByProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMedicineById(id: String): Medicine? {
        return medicineDao.getById(id)?.toDomain()
    }

    override fun searchMedicines(profileId: String, query: String): Flow<List<Medicine>> {
        return medicineDao.searchByName(profileId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMedicinesByCategory(profileId: String, category: MedicineCategory): Flow<List<Medicine>> {
        return medicineDao.getByCategory(profileId, category.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveMedicineNames(profileId: String): List<String> {
        return medicineDao.getActiveMedicineNames(profileId)
    }

    override suspend fun addMedicine(medicine: Medicine) {
        medicineDao.insert(medicine.toEntity())
    }

    override suspend fun updateMedicine(medicine: Medicine) {
        medicineDao.update(medicine.toEntity())
    }

    override suspend fun deleteMedicine(id: String) {
        medicineDao.deactivate(id, System.currentTimeMillis())
    }

    override fun getMedicineCount(profileId: String): Flow<Int> {
        return medicineDao.getCount(profileId)
    }
}
