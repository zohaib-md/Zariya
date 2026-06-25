package com.project.zariya.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.DateTimeUtils
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.usecase.GetMedicinesUseCase
import com.project.zariya.feature.profile.domain.model.UserProfile
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import com.project.zariya.feature.refill.domain.model.RefillInfo
import com.project.zariya.feature.refill.domain.usecase.CheckLowStockUseCase
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.repository.ReminderRepository
import com.project.zariya.feature.reminder.domain.model.Reminder
import com.project.zariya.feature.home.domain.InsightEngine
import com.project.zariya.feature.home.domain.model.HomeInsight
import com.project.zariya.feature.home.domain.model.InsightPriority
import com.project.zariya.feature.reminder.domain.usecase.GetDailyDosesUseCase
import com.project.zariya.feature.reminder.domain.usecase.UpdateDoseStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeProfile: UserProfile? = null,
    val medicines: List<Medicine> = emptyList(),
    val nextReminder: Reminder? = null,
    val todayLogs: List<DoseLog> = emptyList(),
    val lowStockItems: List<RefillInfo> = emptyList(),
    val takenToday: Int = 0,
    val totalToday: Int = 0,
    val currentInsight: HomeInsight? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getActiveProfileUseCase: GetActiveProfileUseCase,
    private val getMedicinesUseCase: GetMedicinesUseCase,
    private val reminderRepository: ReminderRepository,
    private val checkLowStockUseCase: CheckLowStockUseCase,
    private val getDailyDosesUseCase: GetDailyDosesUseCase,
    private val updateDoseStatusUseCase: UpdateDoseStatusUseCase,
    private val insightEngine: InsightEngine
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = getActiveProfileUseCase()
        .flatMapLatest { profile ->
            if (profile == null) {
                flowOf(HomeUiState(isLoading = false))
            } else {
                val startOfDay = DateTimeUtils.todayStartMillis()
                val sevenDaysAgo = startOfDay - (6 * 24 * 60 * 60 * 1000L)
                val endOfDay = startOfDay + (24 * 60 * 60 * 1000L) - 1

                combine(
                    getMedicinesUseCase(profile.id),
                    reminderRepository.getReminders(profile.id),
                    getDailyDosesUseCase(profile.id, startOfDay),
                    checkLowStockUseCase.getLowStockItems(profile.id),
                    reminderRepository.getDoseLogsForDate(profile.id, sevenDaysAgo, endOfDay)
                ) { medicines, reminders, dailyDoses, lowStock, weeklyLogs ->
                    val nextReminder = reminders
                        .filter { it.isActive && it.nextTriggerTime > System.currentTimeMillis() }
                        .minByOrNull { it.nextTriggerTime }

                    val takenToday = dailyDoses.count { it.status == DoseStatus.TAKEN }
                    val totalToday = dailyDoses.size
                    
                    val pastLogs = weeklyLogs.filter { it.scheduledTime <= System.currentTimeMillis() }
                    val weeklyTaken = pastLogs.count { it.status == DoseStatus.TAKEN }
                    val weeklyTotal = pastLogs.count { it.status == DoseStatus.TAKEN || it.status == DoseStatus.MISSED || it.status == DoseStatus.SKIPPED }
                    
                    val calculatedWeeklyAdherence = if (weeklyTotal > 0) {
                        (weeklyTaken.toFloat() / weeklyTotal.toFloat() * 100).toInt()
                    } else {
                        -1 // Signal that we don't have enough data yet
                    }

                    val insight = insightEngine.generateInsight(
                        todayLogs = dailyDoses,
                        lowStockItems = lowStock,
                        weeklyAdherencePercentage = calculatedWeeklyAdherence
                    )
                    
                    HomeUiState(
                        activeProfile = profile,
                        medicines = medicines,
                        nextReminder = nextReminder,
                        todayLogs = dailyDoses,
                        lowStockItems = lowStock,
                        takenToday = takenToday,
                        totalToday = totalToday,
                        currentInsight = insight,
                        isLoading = false
                    )
                }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState(isLoading = true)
            )

    fun onDoseStatusUpdate(doseLog: DoseLog, newStatus: DoseStatus) {
        viewModelScope.launch {
            updateDoseStatusUseCase(doseLog, newStatus)
        }
    }
}
