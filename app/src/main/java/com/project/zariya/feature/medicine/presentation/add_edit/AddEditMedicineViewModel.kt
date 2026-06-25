package com.project.zariya.feature.medicine.presentation.add_edit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import com.project.zariya.feature.medicine.domain.model.MedicineForm
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import com.project.zariya.feature.medicine.domain.usecase.AddMedicineUseCase
import com.project.zariya.feature.medicine.domain.usecase.UpdateMedicineUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditMedicineUiState(
    val name: String = "",
    val genericName: String = "",
    val dosage: String = "",
    val dosageUnit: String = "mg",
    val category: MedicineCategory = MedicineCategory.OTHER,
    val form: MedicineForm = MedicineForm.TABLET,
    val manufacturer: String = "",
    val notes: String = "",
    val prescriptionNotes: String = "",
    val doctorName: String = "",
    val doctorPhone: String = "",
    val prescriptionImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val isStockTracked: Boolean = false,
    val stockCount: String = "",
    val totalVolume: String = "",
    val volumePerDose: String = "",
    val stockAlertThreshold: String = "10",
    val setReminder: Boolean = false,
    val savedMedicineId: String? = null
)

@HiltViewModel
class AddEditMedicineViewModel @Inject constructor(
    private val addMedicineUseCase: AddMedicineUseCase,
    private val updateMedicineUseCase: UpdateMedicineUseCase,
    private val medicineRepository: MedicineRepository,
    private val getActiveProfileUseCase: GetActiveProfileUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["id"]

    private val _uiState = MutableStateFlow(AddEditMedicineUiState())
    val uiState: StateFlow<AddEditMedicineUiState> = _uiState.asStateFlow()

    private var currentMedicine: Medicine? = null

    init {
        medicineId?.let { id ->
            if (id.isNotBlank()) {
                loadMedicine(id)
            }
        }
    }

    private fun loadMedicine(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val medicine = medicineRepository.getMedicineById(id)
            if (medicine != null) {
                currentMedicine = medicine
                _uiState.update {
                    it.copy(
                        name = medicine.name,
                        genericName = medicine.genericName,
                        dosage = medicine.dosage,
                        dosageUnit = medicine.dosageUnit,
                        category = medicine.category,
                        form = medicine.form,
                        manufacturer = medicine.manufacturer,
                        notes = medicine.notes,
                        prescriptionNotes = medicine.prescriptionNotes,
                        doctorName = medicine.doctorName,
                        doctorPhone = medicine.doctorPhone,
                        prescriptionImageUri = medicine.prescriptionImageUri?.let { uri -> Uri.parse(uri) },
                        isStockTracked = medicine.isStockTracked,
                        stockCount = if (medicine.stockCount > 0) medicine.stockCount.toString() else "",
                        totalVolume = medicine.totalVolume?.toString() ?: "",
                        volumePerDose = medicine.volumePerDose?.toString() ?: "",
                        stockAlertThreshold = medicine.stockAlertThreshold.toString(),
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Medicine not found") }
            }
        }
    }

    fun updateState(newState: AddEditMedicineUiState) {
        _uiState.value = newState
    }

    fun saveMedicine() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val profile = getActiveProfileUseCase().firstOrNull()
            if (profile == null) {
                _uiState.update { it.copy(isLoading = false, error = "No active profile found") }
                return@launch
            }

            val state = _uiState.value
            val medicine = Medicine(
                id = currentMedicine?.id ?: "",
                profileId = profile.id,
                name = state.name,
                genericName = state.genericName,
                dosage = state.dosage,
                dosageUnit = state.dosageUnit,
                category = state.category,
                form = state.form,
                manufacturer = state.manufacturer,
                notes = state.notes,
                prescriptionNotes = state.prescriptionNotes,
                doctorName = state.doctorName,
                doctorPhone = state.doctorPhone,
                prescriptionImageUri = state.prescriptionImageUri?.toString(),
                createdAt = currentMedicine?.createdAt ?: 0L,
                updatedAt = 0L,
                isActive = true,
                isStockTracked = state.isStockTracked,
                stockCount = state.stockCount.toIntOrNull() ?: 0,
                totalVolume = state.totalVolume.toIntOrNull(),
                volumePerDose = state.volumePerDose.toIntOrNull(),
                stockAlertThreshold = state.stockAlertThreshold.toIntOrNull() ?: 10
            )

            val result = if (currentMedicine == null) {
                addMedicineUseCase(medicine)
            } else {
                updateMedicineUseCase(medicine)
            }

            when (result) {
                is com.project.zariya.core.util.Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true, savedMedicineId = medicine.id) }
                }
                is com.project.zariya.core.util.Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
