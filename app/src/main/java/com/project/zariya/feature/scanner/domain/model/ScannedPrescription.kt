package com.project.zariya.feature.scanner.domain.model

data class ScannedPrescription(
    val id: String,
    val profileId: String,
    val imageUri: String,
    val rawText: String,
    val extractedMedicines: List<ExtractedMedicine>,
    val scannedAt: Long,
    val isProcessed: Boolean
)

data class ExtractedMedicine(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val confidence: Float
)
