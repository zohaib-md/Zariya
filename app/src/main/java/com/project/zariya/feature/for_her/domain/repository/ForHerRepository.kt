package com.project.zariya.feature.for_her.domain.repository

import com.project.zariya.feature.for_her.data.local.CycleLogEntity
import com.project.zariya.feature.for_her.data.local.ForHerDao
import com.project.zariya.feature.for_her.data.local.WellbeingLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForHerRepository @Inject constructor(
    private val dao: ForHerDao
) {
    suspend fun saveCycleLog(log: CycleLogEntity) {
        dao.insertCycleLog(log)
    }

    fun getCycleLogs(profileId: String): Flow<List<CycleLogEntity>> {
        return dao.getCycleLogs(profileId)
    }

    suspend fun saveWellbeingLog(log: WellbeingLogEntity) {
        dao.insertWellbeingLog(log)
    }

    fun getWellbeingLogs(profileId: String, startMillis: Long, endMillis: Long): Flow<List<WellbeingLogEntity>> {
        return dao.getWellbeingLogs(profileId, startMillis, endMillis)
    }
}
