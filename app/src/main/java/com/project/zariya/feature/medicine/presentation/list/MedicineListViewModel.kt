package com.project.zariya.feature.medicine.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import com.project.zariya.feature.medicine.domain.model.MedicineForm
import com.project.zariya.feature.medicine.domain.usecase.DeleteMedicineUseCase
import com.project.zariya.feature.medicine.domain.usecase.GetMedicinesUseCase
import com.project.zariya.feature.medicine.domain.usecase.UpdateMedicineUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.usecase.UpdateDoseStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

data class EnrichedMedicine(
    val medicine: Medicine,
    val nextDoseTime: Long?,
    val adherencePercentage: Int,
    val stockStatus: StockStatus,
    val isCompletedToday: Boolean
)

data class MedicineListUiState(
    val medicines: List<EnrichedMedicine> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: MedicineCategory? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MedicineListViewModel @Inject constructor(
    private val getMedicinesUseCase: GetMedicinesUseCase,
    private val deleteMedicineUseCase: DeleteMedicineUseCase,
    private val updateMedicineUseCase: UpdateMedicineUseCase,
    private val reminderRepository: ReminderRepository,
    private val updateDoseStatusUseCase: UpdateDoseStatusUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<MedicineCategory?>(null)
    private val _error = MutableStateFlow<String?>(null)

    // Mutex to prevent concurrent mark/unmark operations on the same medicine
    private val actionMutex = Mutex()

    // Track which medicines are currently being processed to block double-taps
    private val processingMedicines = mutableSetOf<String>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MedicineListUiState> = getActiveProfileUseCase()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(MedicineListUiState(isLoading = false, error = "No active profile"))
            } else {
                val filtersFlow = combine(_searchQuery, _selectedCategory, _error) { q, c, e -> Triple(q, c, e) }
                val sevenDaysAgo = DateTimeUtils.todayStartMillis() - (6 * 24 * 60 * 60 * 1000L)
                val endOfDay = DateTimeUtils.todayEndMillis()

                combine(
                    getMedicinesUseCase(profile.id),
                    reminderRepository.getReminders(profile.id),
                    reminderRepository.getDoseLogsForDate(profile.id, sevenDaysAgo, endOfDay),
                    filtersFlow
                ) { medicines, reminders, doseLogs, filters ->
                    val (query, category, error) = filters
                    val filteredMedicines = medicines.filter { medicine ->
                        val matchesQuery = medicine.name.contains(query, ignoreCase = true) ||
                                           medicine.genericName.contains(query, ignoreCase = true)
                        val matchesCategory = category == null || medicine.category == category
                        matchesQuery && matchesCategory
                    }

                    val enrichedMedicines = filteredMedicines.map { medicine ->
                        val medReminders = reminders.filter { it.medicineId == medicine.id && it.isActive }
                        val nextDoseTime = medReminders.map { it.nextTriggerTime }.filter { it > System.currentTimeMillis() }.minOrNull()
                        
                        val medLogs = doseLogs.filter { it.medicineId == medicine.id && it.scheduledTime <= System.currentTimeMillis() }
                        val takenCount = medLogs.count { it.status == DoseStatus.TAKEN }
                        val totalCount = medLogs.count { it.status == DoseStatus.TAKEN || it.status == DoseStatus.MISSED || it.status == DoseStatus.SKIPPED }
                        
                        val adherence = if (totalCount > 0) {
                            (takenCount.toFloat() / totalCount.toFloat() * 100).toInt()
                        } else {
                            -1
                        }

                        val stockStatus = if (!medicine.isStockTracked) {
                            StockStatus.NOT_TRACKED
                        } else if (medicine.currentStock <= 0) {
                            StockStatus.CRITICAL
                        } else if (medicine.currentStock <= medicine.stockAlertThreshold) {
                            StockStatus.LOW
                        } else {
                            StockStatus.HEALTHY
                        }
                        
                        val todayStart = DateTimeUtils.todayStartMillis()
                        val todayLogs = doseLogs.filter { it.medicineId == medicine.id && it.scheduledTime >= todayStart }
                        val hasTakenDoseToday = todayLogs.any { it.status == DoseStatus.TAKEN }
                        val hasPendingDoseToday = todayLogs.any { it.status == DoseStatus.PENDING }
                        val isCompletedToday = hasTakenDoseToday && !hasPendingDoseToday

                        EnrichedMedicine(
                            medicine = medicine,
                            nextDoseTime = nextDoseTime,
                            adherencePercentage = adherence,
                            stockStatus = stockStatus,
                            isCompletedToday = isCompletedToday
                        )
                    }

                    MedicineListUiState(
                        medicines = enrichedMedicines,
                        searchQuery = query,
                        selectedCategory = category,
                        isLoading = false,
                        error = error
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MedicineListUiState(isLoading = true)
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: MedicineCategory?) {
        _selectedCategory.value = category
    }

    fun deleteMedicine(id: String) {
        viewModelScope.launch {
            val result = deleteMedicineUseCase(id)
            if (result is com.project.zariya.core.util.Result.Error) {
                _error.value = result.message
            }
        }
    }
    
    /**
     * Mark a medicine as taken. Idempotent — if already processing, does nothing.
     * Stock deduction happens inside UpdateDoseStatusUseCase ONLY when status becomes TAKEN.
     */
    fun markAsTaken(medicine: Medicine) {
        viewModelScope.launch {
            actionMutex.withLock {
                // Guard against double-tap / duplicate calls
                if (medicine.id in processingMedicines) return@withLock
                processingMedicines.add(medicine.id)
            }

            try {
                val profile = getActiveProfileUseCase().firstOrNull() ?: return@launch
                val todayStart = DateTimeUtils.todayStartMillis()
                val endOfDay = DateTimeUtils.todayEndMillis()
                
                val todayLogs = reminderRepository.getDoseLogsForDate(profile.id, todayStart, endOfDay).firstOrNull() ?: emptyList()
                val pendingLog = todayLogs
                    .filter { it.medicineId == medicine.id && it.status == DoseStatus.PENDING }
                    .minByOrNull { it.scheduledTime }
                    
                if (pendingLog != null) {
                    // Flip existing pending log to TAKEN (UpdateDoseStatusUseCase handles stock)
                    updateDoseStatusUseCase(pendingLog, DoseStatus.TAKEN)
                } else {
                    // No pending log exists — check if already taken today to avoid duplicates
                    val alreadyTakenToday = todayLogs.any { it.medicineId == medicine.id && it.status == DoseStatus.TAKEN }
                    val hasPending = todayLogs.any { it.medicineId == medicine.id && it.status == DoseStatus.PENDING }
                    if (alreadyTakenToday && !hasPending) {
                        // Already completed for today, don't create another log
                        return@launch
                    }

                    // Create a virtual log for medicines without a schedule
                    val reminders = reminderRepository.getReminders(profile.id).firstOrNull() ?: emptyList()
                    val firstReminder = reminders.firstOrNull { it.medicineId == medicine.id }
                    
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
                        profileId = profile.id,
                        medicineId = medicine.id,
                        medicineName = medicine.name,
                        reminderId = finalReminderId,
                        scheduledTime = System.currentTimeMillis(),
                        status = DoseStatus.PENDING
                    )
                    updateDoseStatusUseCase(virtualLog, DoseStatus.TAKEN)
                }
            } finally {
                actionMutex.withLock {
                    processingMedicines.remove(medicine.id)
                }
            }
        }
    }

    /**
     * Undo a taken dose. Reverts the most recent TAKEN log back to PENDING,
     * and manually restores stock since UpdateDoseStatusUseCase only deducts on TAKEN.
     */
    fun unmarkAsTaken(medicine: Medicine) {
        viewModelScope.launch {
            actionMutex.withLock {
                if (medicine.id in processingMedicines) return@withLock
                processingMedicines.add(medicine.id)
            }

            try {
                val profile = getActiveProfileUseCase().firstOrNull() ?: return@launch
                val todayStart = DateTimeUtils.todayStartMillis()
                val endOfDay = DateTimeUtils.todayEndMillis()
                
                val todayLogs = reminderRepository.getDoseLogsForDate(profile.id, todayStart, endOfDay).firstOrNull() ?: emptyList()
                val takenLog = todayLogs
                    .filter { it.medicineId == medicine.id && it.status == DoseStatus.TAKEN }
                    .maxByOrNull { it.actionTime ?: 0L }
                    
                if (takenLog != null) {
                    // Revert log status to PENDING (this does NOT auto-restore stock)
                    val updatedLog = takenLog.copy(status = DoseStatus.PENDING, actionTime = null)
                    reminderRepository.logDose(updatedLog)
                    
                    // Manually restore stock — use the correct unit type
                    if (medicine.isStockTracked) {
                        val isLiquid = medicine.form == MedicineForm.SYRUP ||
                                       medicine.form == MedicineForm.DROPS ||
                                       medicine.form == MedicineForm.INJECTION
                        if (isLiquid) {
                            val volumeToRestore = medicine.volumePerDose ?: when (medicine.form) {
                                MedicineForm.SYRUP -> 5
                                MedicineForm.DROPS -> 1
                                else -> 1
                            }
                            val updatedMedicine = medicine.copy(
                                totalVolume = (medicine.totalVolume ?: 0) + volumeToRestore
                            )
                            updateMedicineUseCase(updatedMedicine)
                        } else {
                            val updatedMedicine = medicine.copy(
                                stockCount = medicine.stockCount + 1
                            )
                            updateMedicineUseCase(updatedMedicine)
                        }
                    }
                }
            } finally {
                actionMutex.withLock {
                    processingMedicines.remove(medicine.id)
                }
            }
        }
    }

    /**
     * Add stock to a medicine. Only triggered by explicit user action (tap on Add Stock button).
     */
    fun addStock(medicine: Medicine, amountToAdd: Int) {
        viewModelScope.launch {
            val isLiquid = medicine.form == MedicineForm.SYRUP ||
                           medicine.form == MedicineForm.DROPS ||
                           medicine.form == MedicineForm.INJECTION
            val updatedMedicine = if (isLiquid) {
                medicine.copy(
                    totalVolume = (medicine.totalVolume ?: 0) + amountToAdd,
                    isStockTracked = true
                )
            } else {
                medicine.copy(
                    stockCount = medicine.stockCount + amountToAdd,
                    isStockTracked = true
                )
            }
            updateMedicineUseCase(updatedMedicine)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
