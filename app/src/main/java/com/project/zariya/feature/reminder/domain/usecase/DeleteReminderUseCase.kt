package com.project.zariya.feature.reminder.domain.usecase

import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(reminderId: String) {
        repository.deleteReminder(reminderId)
        alarmScheduler.cancelReminder(reminderId)
    }
}
