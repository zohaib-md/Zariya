package com.project.zariya.feature.interaction.domain.repository

import com.project.zariya.core.util.Result
import com.project.zariya.feature.interaction.data.remote.DrugLabelResult
import com.project.zariya.feature.interaction.domain.model.DrugInteraction

interface InteractionRepository {
    suspend fun checkInteraction(drug1: String, drug2: String): Result<List<DrugInteraction>>
    suspend fun getDrugInfo(drugName: String): Result<DrugLabelResult?>
    suspend fun checkAllInteractions(medicines: List<String>): Result<List<DrugInteraction>>
}
