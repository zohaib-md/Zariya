package com.project.zariya.feature.reminder.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.usecase.GetRemindersUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderListUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReminderListViewModel @Inject constructor(
    private val getRemindersUseCase: GetRemindersUseCase,
    private val scheduleReminderUseCase: com.project.zariya.feature.reminder.domain.usecase.ScheduleReminderUseCase,
    private val deleteReminderUseCase: com.project.zariya.feature.reminder.domain.usecase.DeleteReminderUseCase,
    getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReminderListUiState> = getActiveProfileUseCase()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(ReminderListUiState(isLoading = false, error = "No active profile"))
            } else {
                getRemindersUseCase(profile.id).map { reminders ->
                    ReminderListUiState(
                        reminders = reminders,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReminderListUiState(isLoading = true)
        )

    fun toggleReminder(reminder: Reminder, isActive: Boolean) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(isActive = isActive)
            // ScheduleReminderUseCase updates the database and schedules/cancels the alarm
            scheduleReminderUseCase(updatedReminder)
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ReminderListViewModel", "Attempting to delete reminder $reminderId")
                deleteReminderUseCase(reminderId)
                android.util.Log.d("ReminderListViewModel", "Successfully deleted reminder $reminderId")
            } catch (e: Exception) {
                android.util.Log.e("ReminderListViewModel", "Error deleting reminder", e)
            }
        }
    }
}
