package com.project.zariya.feature.medicine.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryHealthUiState(
    val trackedMedicines: List<Medicine> = emptyList(),
    val isLoading: Boolean = true
) {
    val healthyMedicines = trackedMedicines.filter { it.currentStock > it.stockAlertThreshold * 2 }
    val mediumMedicines = trackedMedicines.filter { it.currentStock > it.stockAlertThreshold && it.currentStock <= it.stockAlertThreshold * 2 }
    val criticalMedicines = trackedMedicines.filter { it.currentStock <= it.stockAlertThreshold }
}

@HiltViewModel
class InventoryHealthViewModel @Inject constructor(
    private val medicineRepository: MedicineRepository,
    private val getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryHealthUiState())
    val uiState: StateFlow<InventoryHealthUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            val activeProfile = getActiveProfileUseCase.invoke().firstOrNull()
            if (activeProfile != null) {
                medicineRepository.getMedicines(activeProfile.id).collect { medicines ->
                    _uiState.update { 
                        it.copy(
                            trackedMedicines = medicines.filter { med -> med.isStockTracked },
                            isLoading = false
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
