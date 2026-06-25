package com.project.zariya.feature.medicine.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import com.project.zariya.feature.medicine.domain.usecase.DeleteMedicineUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.domain.usecase.GetDailyDosesUseCase
import com.project.zariya.feature.reminder.domain.usecase.UpdateDoseStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MedicineDetailUiState(
    val medicine: Medicine? = null,
    val reminders: List<Reminder> = emptyList(),
    val recentDoseLogs: List<DoseLog> = emptyList(),
    val adherenceWeekly: Float = -1f,
    val adherenceMonthly: Float = -1f,
    val upcomingDoses: List<Long> = emptyList(),
    val estimatedDaysRemaining: Int = -1,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    private val repository: MedicineRepository,
    private val reminderRepository: ReminderRepository,
    private val deleteMedicineUseCase: DeleteMedicineUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    private val updateDoseStatusUseCase: UpdateDoseStatusUseCase,
    private val getDailyDosesUseCase: GetDailyDosesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineDetailUiState())
    val uiState: StateFlow<MedicineDetailUiState> = _uiState.asStateFlow()

    fun loadMedicine(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val profileFlow = getActiveProfileUseCase()
                
                profileFlow.flatMapLatest { profile ->
                    if (profile == null) return@flatMapLatest flowOf(MedicineDetailUiState(error = "No active profile", isLoading = false))
                    
                    combine(
                        repository.getMedicines(profile.id).map { list -> list.find { m -> m.id == id } },
                        reminderRepository.getRemindersByMedicine(id),
                        reminderRepository.getDoseLogsForMedicine(id),
                        getDailyDosesUseCase(profile.id, System.currentTimeMillis())
                    ) { medicine, reminders, doseLogs, todayDoses ->
                        if (medicine == null) {
                            return@combine MedicineDetailUiState(error = "Medicine not found", isLoading = false)
                        }

                        val now = System.currentTimeMillis()
                        
                        val upcomingDoses = todayDoses
                            .filter { it.medicineId == id && it.scheduledTime >= now }
                            .map { it.scheduledTime }
                            .sorted()
                            .take(3)

                    // Deduplicate logs created by the previous notification intent bug
                    val uniqueLogs = doseLogs.distinctBy { it.reminderId + "_" + (it.scheduledTime / 300000L) }

                    // Calculate adherence
                    val oneWeekAgo = now - 7L * 24 * 60 * 60 * 1000
                    val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000

                    val weeklyLogs = uniqueLogs.filter { (it.actionTime ?: it.scheduledTime) >= oneWeekAgo }
                    val monthlyLogs = uniqueLogs.filter { (it.actionTime ?: it.scheduledTime) >= oneMonthAgo }

                    val adherenceWeekly = calculateAdherence(weeklyLogs)
                    val adherenceMonthly = calculateAdherence(monthlyLogs)

                    // Calculate estimated days remaining
                    val estimatedDaysRemaining = calculateEstimatedDaysRemaining(medicine, reminders)

                    MedicineDetailUiState(
                        medicine = medicine,
                        reminders = reminders,
                        recentDoseLogs = uniqueLogs.take(5), // Top 5 recent
                        adherenceWeekly = adherenceWeekly,
                        adherenceMonthly = adherenceMonthly,
                        upcomingDoses = upcomingDoses,
                        estimatedDaysRemaining = estimatedDaysRemaining,
                        isLoading = false,
                        error = null
                    )
                    }
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load medicine"
                )
            }
        }
    }

    private fun calculateAdherence(logs: List<DoseLog>): Float {
        if (logs.isEmpty()) return -1f
        val takenOrSkipped = logs.count { it.status == DoseStatus.TAKEN || it.status == DoseStatus.SKIPPED }
        val totalPast = logs.count { it.status != DoseStatus.PENDING }
        if (totalPast == 0) return -1f
        return (takenOrSkipped.toFloat() / totalPast) * 100f
    }

    private fun calculateEstimatedDaysRemaining(medicine: Medicine, reminders: List<Reminder>): Int {
        if (!medicine.isStockTracked || medicine.stockCount <= 0) return -1
        
        // Doses per day based on reminders
        var dosesPerDay = 0f
        reminders.filter { it.isActive }.forEach { reminder ->
            val intervalHours = reminder.intervalHours
            if (intervalHours > 0) {
                dosesPerDay += 24f / intervalHours
            } else {
                dosesPerDay += 1f // fallback
            }
        }
        
        if (dosesPerDay == 0f) return -1 // No active reminders, can't estimate
        
        val stock = if (medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.SYRUP ||
                        medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.DROPS ||
                        medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.INJECTION) {
            medicine.totalVolume ?: 0
        } else {
            medicine.stockCount
        }
        
        return (stock / dosesPerDay).toInt()
    }

    fun deleteMedicine() {
        val medicine = _uiState.value.medicine ?: return
        viewModelScope.launch {
            val result = deleteMedicineUseCase(medicine.id)
            if (result is com.project.zariya.core.util.Result.Error) {
                _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    fun addStock(amount: Int) {
        val medicine = _uiState.value.medicine ?: return
        viewModelScope.launch {
            val updatedMedicine = if (medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.SYRUP ||
                                      medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.DROPS ||
                                      medicine.form == com.project.zariya.feature.medicine.domain.model.MedicineForm.INJECTION) {
                medicine.copy(
                    totalVolume = (medicine.totalVolume ?: 0) + amount,
                    isStockTracked = true
                )
            } else {
                medicine.copy(
                    stockCount = medicine.stockCount + amount,
                    isStockTracked = true
                )
            }
            try {
                repository.updateMedicine(updatedMedicine)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update stock")
            }
        }
    }

    fun markAsTaken() {
        val medicine = _uiState.value.medicine ?: return
        viewModelScope.launch {
            val profile = getActiveProfileUseCase().firstOrNull() ?: return@launch
            val todayStart = DateTimeUtils.todayStartMillis()
            val endOfDay = DateTimeUtils.todayEndMillis()

            val todayLogs = reminderRepository.getDoseLogsForDate(profile.id, todayStart, endOfDay).firstOrNull() ?: emptyList()
            val pendingLog = todayLogs
                .filter { it.medicineId == medicine.id && it.status == DoseStatus.PENDING }
                .minByOrNull { it.scheduledTime }
                
            if (pendingLog != null) {
                updateDoseStatusUseCase(pendingLog, DoseStatus.TAKEN)
            } else {
                val alreadyTakenToday = todayLogs.any { it.medicineId == medicine.id && it.status == DoseStatus.TAKEN }
                val hasPending = todayLogs.any { it.medicineId == medicine.id && it.status == DoseStatus.PENDING }
                if (alreadyTakenToday && !hasPending) return@launch

                val reminders = reminderRepository.getRemindersByMedicine(medicine.id).firstOrNull() ?: emptyList()
                val firstReminder = reminders.firstOrNull()
                
                val finalReminderId = if (firstReminder != null) {
                    firstReminder.id
                } else {
                    val dummyId = "dummy_${medicine.id}"
                    val existingDummy = reminderRepository.getReminderById(dummyId)
                    if (existingDummy == null) {
                        val dummyReminder = com.project.zariya.feature.reminder.domain.model.Reminder(
                            id = dummyId,
                            medicineId = medicine.id,
                            medicineName = medicine.name,
                            profileId = profile.id,
                            scheduleType = com.project.zariya.feature.reminder.domain.model.ScheduleType.DAILY,
                            scheduledTimes = emptyList(),
                            selectedDays = emptyList(),
                            intervalHours = 0,
                            startDate = System.currentTimeMillis(),
                            endDate = null,
                            nextTriggerTime = System.currentTimeMillis(),
                            isActive = false
                        )
                        reminderRepository.addReminder(dummyReminder)
                    }
                    dummyId
                }

                val virtualLog = DoseLog(
                    id = "virtual_${UUID.randomUUID()}",
                    reminderId = finalReminderId,
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    profileId = profile.id,
                    scheduledTime = System.currentTimeMillis(),
                    actionTime = System.currentTimeMillis(),
                    status = DoseStatus.PENDING,
                    snoozedCount = 0
                )
                updateDoseStatusUseCase(virtualLog, DoseStatus.TAKEN)
            }
        }
    }

    fun toggleReminders(isActive: Boolean) {
        val medicine = _uiState.value.medicine ?: return
        viewModelScope.launch {
            try {
                repository.updateMedicine(medicine.copy(isActive = isActive))
                
                // Also deactivate/activate reminders
                val reminders = reminderRepository.getRemindersByMedicine(medicine.id).firstOrNull() ?: emptyList()
                reminders.forEach { reminder ->
                    if (isActive) {
                        reminderRepository.updateReminder(reminder.copy(isActive = true))
                    } else {
                        reminderRepository.deactivateReminder(reminder.id)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to toggle status")
            }
        }
    }
}
