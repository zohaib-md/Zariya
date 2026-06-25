package com.project.zariya.feature.health.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.Result
import com.project.zariya.feature.health.data.HealthConnectManager
import com.project.zariya.feature.health.domain.model.HealthMetricType
import com.project.zariya.feature.health.domain.model.HealthRecord
import com.project.zariya.feature.health.domain.usecase.GetHealthDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HealthDashboardUiState(
    val isAvailable: Boolean = false,
    val hasPermission: Boolean = false,
    val latestMetrics: Map<HealthMetricType, HealthRecord?> = emptyMap(),
    val bloodPressureHistory: List<HealthRecord.BloodPressureRecord> = emptyList(),
    val weightHistory: List<HealthRecord.WeightRecord> = emptyList(),
    val glucoseHistory: List<HealthRecord.BloodGlucoseRecord> = emptyList(),
    val heartRateHistory: List<HealthRecord.HeartRateRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HealthDashboardViewModel @Inject constructor(
    private val getHealthDataUseCase: GetHealthDataUseCase,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthDashboardUiState())
    val uiState: StateFlow<HealthDashboardUiState> = _uiState.asStateFlow()

    init {
        checkAvailability()
    }

    fun checkAvailability() {
        val available = healthConnectManager.isAvailable()
        _uiState.update { it.copy(isAvailable = available) }
        if (available) {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        viewModelScope.launch {
            val permissions = healthConnectManager.getRequiredPermissions()
            val hasPerms = healthConnectManager.hasPermissions(permissions)
            _uiState.update { it.copy(hasPermission = hasPerms) }
            if (hasPerms) {
                loadData()
            }
        }
    }

    fun onPermissionsResult() {
        checkPermissions()
    }

    fun requestPermissions(): Set<String> {
        return healthConnectManager.getRequiredPermissions()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val latestResult = getHealthDataUseCase.getLatestMetrics()
            val bpResult = getHealthDataUseCase.getBloodPressureHistory()
            val weightResult = getHealthDataUseCase.getWeightHistory()
            val glucoseResult = getHealthDataUseCase.getBloodGlucoseHistory()
            val heartRateResult = getHealthDataUseCase.getHeartRateHistory()

            when (latestResult) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            latestMetrics = latestResult.data,
                            bloodPressureHistory = when (bpResult) {
                                is Result.Success -> bpResult.data
                                else -> state.bloodPressureHistory
                            },
                            weightHistory = when (weightResult) {
                                is Result.Success -> weightResult.data
                                else -> state.weightHistory
                            },
                            glucoseHistory = when (glucoseResult) {
                                is Result.Success -> glucoseResult.data
                                else -> state.glucoseHistory
                            },
                            heartRateHistory = when (heartRateResult) {
                                is Result.Success -> heartRateResult.data
                                else -> state.heartRateHistory
                            },
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = latestResult.message
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled via isLoading flag
                }
            }
        }
    }

    fun refreshData() {
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
