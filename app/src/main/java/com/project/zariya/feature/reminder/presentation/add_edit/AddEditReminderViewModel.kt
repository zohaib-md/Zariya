package com.project.zariya.feature.reminder.presentation.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.Result
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.usecase.GetMedicinesUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.model.ScheduleType
import com.project.zariya.feature.reminder.domain.usecase.ScheduleReminderUseCase
import androidx.lifecycle.SavedStateHandle
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditReminderUiState(
    val selectedMedicineId: String = "",
    val selectedMedicineName: String = "",
    val scheduleType: ScheduleType = ScheduleType.DAILY,
    val scheduledTimes: List<String> = listOf("08:00 AM"),
    val selectedDays: List<Int> = emptyList(),
    val intervalHours: Int = 8,
    val dosage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val isActive: Boolean = true
)

@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    private val scheduleReminderUseCase: ScheduleReminderUseCase,
    private val getMedicinesUseCase: GetMedicinesUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val reminderRepository: ReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reminderId: String? = savedStateHandle["id"]
    private val preselectedMedicineId: String? = savedStateHandle["medicineId"]
    private var currentReminder: Reminder? = null

    private val _uiState = MutableStateFlow(AddEditReminderUiState())
    val uiState: StateFlow<AddEditReminderUiState> = _uiState.asStateFlow()



    private fun loadReminder(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val reminder = reminderRepository.getReminderById(id)
            if (reminder != null) {
                currentReminder = reminder
                _uiState.update {
                    it.copy(
                        selectedMedicineId = reminder.medicineId,
                        selectedMedicineName = reminder.medicineName,
                        scheduleType = reminder.scheduleType,
                        scheduledTimes = reminder.scheduledTimes,
                        selectedDays = reminder.selectedDays,
                        intervalHours = reminder.intervalHours,
                        dosage = reminder.dosage,
                        isActive = reminder.isActive,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Reminder not found") }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val medicines: StateFlow<List<Medicine>> = getActiveProfileUseCase()
        .flatMapLatest { profile ->
            if (profile == null) flowOf(emptyList())
            else getMedicinesUseCase(profile.id)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        if (!reminderId.isNullOrBlank()) {
            loadReminder(reminderId)
        } else if (!preselectedMedicineId.isNullOrBlank()) {
            viewModelScope.launch {
                medicines.collect { meds ->
                    val med = meds.find { it.id == preselectedMedicineId }
                    if (med != null && _uiState.value.selectedMedicineId.isBlank()) {
                        onMedicineSelected(med)
                    }
                }
            }
        }
    }

    fun onMedicineSelected(medicine: Medicine) {
        _uiState.update {
            it.copy(
                selectedMedicineId = medicine.id,
                selectedMedicineName = medicine.name,
                dosage = "${medicine.dosage} ${medicine.dosageUnit}"
            )
        }
    }

    fun onScheduleTypeChange(scheduleType: ScheduleType) {
        _uiState.update {
            it.copy(
                scheduleType = scheduleType,
                selectedDays = if (scheduleType == ScheduleType.SPECIFIC_DAYS) it.selectedDays else emptyList(),
                intervalHours = if (scheduleType == ScheduleType.EVERY_N_HOURS) it.intervalHours else 8
            )
        }
    }

    fun addTime(time: String) {
        _uiState.update {
            it.copy(scheduledTimes = it.scheduledTimes + time)
        }
    }

    fun updateTime(index: Int, time: String) {
        _uiState.update {
            val newTimes = it.scheduledTimes.toMutableList()
            if (index in newTimes.indices) {
                newTimes[index] = time
            }
            it.copy(scheduledTimes = newTimes)
        }
    }

    fun removeTime(index: Int) {
        _uiState.update {
            if (it.scheduledTimes.size > 1) {
                it.copy(scheduledTimes = it.scheduledTimes.toMutableList().apply { removeAt(index) })
            } else {
                it.copy(error = "At least one time is required")
            }
        }
    }

    fun toggleDay(day: Int) {
        _uiState.update { state ->
            val updatedDays = if (day in state.selectedDays) {
                state.selectedDays - day
            } else {
                state.selectedDays + day
            }
            state.copy(selectedDays = updatedDays.sorted())
        }
    }

    fun onIntervalHoursChange(hours: String) {
        val parsedHours = hours.toIntOrNull() ?: return
        if (parsedHours in 1..24) {
            _uiState.update { it.copy(intervalHours = parsedHours) }
        }
    }

    fun onDosageChange(dosage: String) {
        _uiState.update { it.copy(dosage = dosage) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun toggleActive() {
        _uiState.update { it.copy(isActive = !it.isActive) }
    }

    fun onSave() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val profile = getActiveProfileUseCase().firstOrNull()
            if (profile == null) {
                _uiState.update { it.copy(isLoading = false, error = "No active profile found") }
                return@launch
            }

            val state = _uiState.value

            if (state.selectedMedicineId.isBlank()) {
                _uiState.update { it.copy(isLoading = false, error = "Please select a medicine") }
                return@launch
            }

            val selectedMedicine = medicines.value.find { it.id == state.selectedMedicineId }

            val reminder = Reminder(
                id = currentReminder?.id ?: "",
                medicineId = state.selectedMedicineId,
                medicineName = state.selectedMedicineName,
                profileId = profile.id,
                scheduleType = state.scheduleType,
                scheduledTimes = state.scheduledTimes,
                selectedDays = state.selectedDays,
                intervalHours = state.intervalHours,
                dosage = state.dosage,
                isActive = state.isActive,
                medicineForm = selectedMedicine?.form?.name ?: "TABLET"
            )

            when (val result = scheduleReminderUseCase(reminder)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
