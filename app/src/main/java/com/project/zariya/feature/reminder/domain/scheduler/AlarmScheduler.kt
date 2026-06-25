package com.project.zariya.feature.reminder.domain.scheduler

import com.project.zariya.feature.reminder.domain.model.Reminder

/**
 * Interface for scheduling, cancelling, and rescheduling medicine reminder alarms.
 * Abstracts the underlying alarm mechanism (AlarmManager) from the domain layer.
 */
interface AlarmScheduler {

    /**
     * Schedules an exact alarm for the given reminder.
     * The alarm will trigger at the next calculated time based on the reminder's schedule type.
     *
     * @param reminder The reminder to schedule an alarm for.
     */
    fun scheduleReminder(reminder: Reminder)

    /**
     * Cancels any pending alarm associated with the given reminder ID.
     *
     * @param reminderId The unique identifier of the reminder whose alarm should be cancelled.
     */
    fun cancelReminder(reminderId: String)

    /**
     * Reschedules all active reminders. Typically called after device reboot
     * or app update to restore alarms that were cleared by the system.
     */
    suspend fun rescheduleAllReminders()
}
