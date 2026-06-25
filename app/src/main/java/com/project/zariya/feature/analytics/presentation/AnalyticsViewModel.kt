package com.project.zariya.feature.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.feature.analytics.domain.model.AdherenceStats
import com.project.zariya.feature.analytics.domain.model.DailyAdherence
import com.project.zariya.feature.analytics.domain.model.Period
import com.project.zariya.feature.analytics.domain.model.WeeklyReport
import com.project.zariya.feature.analytics.domain.usecase.GetAdherenceStatsUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val todayStats: AdherenceStats? = null,
    val weeklyReport: WeeklyReport? = null,
    val monthlyAdherence: List<DailyAdherence> = emptyList(),
    val selectedPeriod: Period = Period.WEEK,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAdherenceStatsUseCase: GetAdherenceStatsUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAllStats()
    }

    fun selectPeriod(period: Period) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }

    fun refreshStats() {
        loadAllStats()
    }

    private fun loadAllStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val profile = getActiveProfileUseCase().first()
                if (profile == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No active profile")
                    return@launch
                }

                val today = getAdherenceStatsUseCase.getToday(profile.id)
                val weekly = getAdherenceStatsUseCase.getWeekly(profile.id)
                val monthly = getAdherenceStatsUseCase.getMonthly(profile.id)

                _uiState.value = _uiState.value.copy(
                    todayStats = today,
                    weeklyReport = weekly,
                    monthlyAdherence = monthly,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load analytics"
                )
            }
        }
    }
}
