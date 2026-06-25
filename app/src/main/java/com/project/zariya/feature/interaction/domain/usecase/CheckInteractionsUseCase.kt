package com.project.zariya.feature.interaction.domain.usecase

import com.project.zariya.core.util.Result
import com.project.zariya.feature.interaction.domain.model.DrugInteraction
import com.project.zariya.feature.interaction.domain.repository.InteractionRepository
import com.project.zariya.feature.medicine.domain.repository.MedicineRepository
import javax.inject.Inject

class CheckInteractionsUseCase @Inject constructor(
    private val interactionRepository: InteractionRepository,
    private val medicineRepository: MedicineRepository
) {

    suspend operator fun invoke(profileId: String): Result<List<DrugInteraction>> {
        return try {
            val medicineNames = medicineRepository.getActiveMedicineNames(profileId)

            if (medicineNames.size < 2) {
                return Result.Success(emptyList())
            }

            interactionRepository.checkAllInteractions(medicineNames)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to check interactions",
                throwable = e
            )
        }
    }

    suspend fun checkPair(drug1: String, drug2: String): Result<List<DrugInteraction>> {
        return interactionRepository.checkInteraction(drug1, drug2)
    }
}
