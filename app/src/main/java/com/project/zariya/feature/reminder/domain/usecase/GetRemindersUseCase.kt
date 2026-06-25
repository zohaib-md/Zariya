package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {

    operator fun invoke(profileId: String): Flow<List<Reminder>> {
        return repository.getReminders(profileId)
    }
}
