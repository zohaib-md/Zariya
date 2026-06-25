package com.project.zariya.feature.interaction.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.Result
import com.project.zariya.feature.interaction.domain.model.DrugInteraction
import com.project.zariya.feature.interaction.domain.usecase.CheckInteractionsUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InteractionUiState(
    val interactions: List<DrugInteraction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val checkedMedicines: List<String> = emptyList()
)

@HiltViewModel
class InteractionViewModel @Inject constructor(
    private val checkInteractionsUseCase: CheckInteractionsUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InteractionUiState())
    val uiState: StateFlow<InteractionUiState> = _uiState.asStateFlow()

    fun checkAllInteractions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val activeProfile = getActiveProfileUseCase().firstOrNull()
            if (activeProfile == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No active profile found. Please select a profile first."
                    )
                }
                return@launch
            }

            when (val result = checkInteractionsUseCase(activeProfile.id)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            interactions = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun checkPair(drug1: String, drug2: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    checkedMedicines = listOf(drug1, drug2)
                )
            }

            when (val result = checkInteractionsUseCase.checkPair(drug1, drug2)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            interactions = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
