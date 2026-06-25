package com.project.zariya.feature.scanner.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.zariya.core.util.Result
import com.project.zariya.feature.scanner.data.ocr.TextRecognitionProcessor
import com.project.zariya.feature.scanner.data.parser.PrescriptionParser
import com.project.zariya.feature.scanner.domain.model.ExtractedMedicine
import com.project.zariya.feature.medicine.domain.model.Medicine
import com.project.zariya.feature.medicine.domain.model.MedicineCategory
import com.project.zariya.feature.medicine.domain.model.MedicineForm
import com.project.zariya.feature.medicine.domain.usecase.AddMedicineUseCase
import com.project.zariya.feature.profile.domain.usecase.GetActiveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val isScanning: Boolean = false,
    val capturedImageUri: Uri? = null,
    val rawText: String = "",
    val extractedMedicines: List<ExtractedMedicine> = emptyList(),
    val selectedMedicines: Set<ExtractedMedicine> = emptySet(),
    val isProcessing: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val textRecognitionProcessor: TextRecognitionProcessor,
    private val prescriptionParser: PrescriptionParser,
    private val addMedicineUseCase: AddMedicineUseCase,
    private val getActiveProfileUseCase: GetActiveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun onImageCaptured(uri: Uri) {
        _uiState.update {
            it.copy(
                capturedImageUri = uri,
                isProcessing = true,
                error = null,
                rawText = "",
                extractedMedicines = emptyList(),
                selectedMedicines = emptySet()
            )
        }

        viewModelScope.launch {
            textRecognitionProcessor.processUri(uri).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isProcessing = true) }
                    }
                    is Result.Success -> {
                        val rawText = result.data
                        val medicines = prescriptionParser.parseText(rawText)
                        _uiState.update {
                            it.copy(
                                rawText = rawText,
                                extractedMedicines = medicines,
                                isProcessing = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onTextRecognized(text: String) {
        val medicines = prescriptionParser.parseText(text)
        _uiState.update {
            it.copy(
                rawText = text,
                extractedMedicines = medicines,
                isProcessing = false,
                error = null
            )
        }
    }

    fun onMedicineSelected(medicine: ExtractedMedicine) {
        _uiState.update { currentState ->
            val updatedSelection = currentState.selectedMedicines.toMutableSet()
            if (updatedSelection.contains(medicine)) {
                updatedSelection.remove(medicine)
            } else {
                updatedSelection.add(medicine)
            }
            currentState.copy(selectedMedicines = updatedSelection)
        }
    }

    fun selectAllMedicines() {
        _uiState.update {
            it.copy(selectedMedicines = it.extractedMedicines.toSet())
        }
    }

    fun deselectAllMedicines() {
        _uiState.update {
            it.copy(selectedMedicines = emptySet())
        }
    }

    fun retryProcessing() {
        val currentUri = _uiState.value.capturedImageUri
        if (currentUri != null) {
            onImageCaptured(currentUri)
        } else {
            _uiState.update {
                it.copy(error = "No image available to retry. Please capture a new image.")
            }
        }
    }

    fun resetScanner() {
        _uiState.update { ScannerUiState() }
    }

    fun saveSelectedMedicines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            val profile = getActiveProfileUseCase().firstOrNull()
            if (profile == null) {
                _uiState.update { it.copy(isProcessing = false, error = "No active profile found") }
                return@launch
            }

            val selected = _uiState.value.selectedMedicines
            var hasError = false
            for (extracted in selected) {
                val medicine = Medicine(
                    id = "",
                    profileId = profile.id,
                    name = extracted.name,
                    genericName = "",
                    dosage = extracted.dosage,
                    dosageUnit = "",
                    category = MedicineCategory.OTHER,
                    form = MedicineForm.TABLET,
                    manufacturer = "",
                    notes = "Frequency: ${extracted.frequency}\nDuration: ${extracted.duration}".trim(),
                    prescriptionNotes = "Extracted with Confidence: ${(extracted.confidence * 100).toInt()}%",
                    doctorName = "",
                    doctorPhone = "",
                    prescriptionImageUri = _uiState.value.capturedImageUri?.toString(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isActive = true
                )
                val result = addMedicineUseCase(medicine)
                if (result is com.project.zariya.core.util.Result.Error) {
                    hasError = true
                    _uiState.update { it.copy(error = result.message) }
                    break
                }
            }

            if (!hasError) {
                _uiState.update { it.copy(isProcessing = false, isSaved = true) }
            } else {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
