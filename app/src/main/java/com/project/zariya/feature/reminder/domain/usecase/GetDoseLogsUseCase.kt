package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDoseLogsUseCase @Inject constructor(
    private val repository: ReminderRepository
) {

    fun forDate(profileId: String, startOfDay: Long, endOfDay: Long): Flow<List<DoseLog>> {
        return repository.getDoseLogsForDate(profileId, startOfDay, endOfDay)
    }

    fun recent(profileId: String, limit: Int = 20): Flow<List<DoseLog>> {
        return repository.getRecentDoseLogs(profileId, limit)
    }
}
