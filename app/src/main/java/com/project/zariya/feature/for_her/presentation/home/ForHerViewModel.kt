package com.project.zariya.feature.for_her.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import com.project.zariya.feature.for_her.data.local.CycleLogEntity
import com.project.zariya.feature.for_her.domain.repository.ForHerRepository
import com.project.zariya.feature.for_her.domain.util.CyclePhase
import com.project.zariya.feature.for_her.domain.util.CyclePhaseCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForHerUiState(
    val currentPhase: CyclePhase = CyclePhase.UNKNOWN,
    val latestCycleLog: CycleLogEntity? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ForHerViewModel @Inject constructor(
    private val repository: ForHerRepository,
    private val getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForHerUiState())
    val uiState: StateFlow<ForHerUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val activeProfile = getActiveProfileUseCase.invoke().firstOrNull()
            val activeProfileId = activeProfile?.id
            
            if (activeProfileId != null) {
                repository.getCycleLogs(activeProfileId).collect { logs ->
                    val latestLog = logs.firstOrNull()
                    val phase = latestLog?.startDateMillis?.let { 
                        CyclePhaseCalculator.calculatePhase(it) 
                    } ?: CyclePhase.UNKNOWN
                    
                    _uiState.update { 
                        it.copy(
                            latestCycleLog = latestLog,
                            currentPhase = phase,
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveCycleLog(startDateMillis: Long, endDateMillis: Long?) {
        viewModelScope.launch {
            val activeProfile = getActiveProfileUseCase.invoke().firstOrNull()
            val activeProfileId = activeProfile?.id ?: return@launch
            
            val log = CycleLogEntity(
                profileId = activeProfileId,
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis
            )
            repository.saveCycleLog(log)
        }
    }
}
