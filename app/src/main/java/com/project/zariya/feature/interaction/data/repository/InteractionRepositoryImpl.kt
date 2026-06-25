package com.project.zariya.feature.interaction.data.repository

import com.project.zariya.core.util.Result
import com.project.zariya.feature.interaction.data.remote.DrugLabelResult
import com.project.zariya.feature.interaction.data.remote.OpenFdaApi
import com.project.zariya.feature.interaction.domain.model.DrugInteraction
import com.project.zariya.feature.interaction.domain.model.InteractionSeverity
import com.project.zariya.feature.interaction.domain.repository.InteractionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionRepositoryImpl @Inject constructor(
    private val openFdaApi: OpenFdaApi
) : InteractionRepository {

    override suspend fun checkInteraction(
        drug1: String,
        drug2: String
    ): Result<List<DrugInteraction>> {
        return try {
            val interactions = mutableListOf<DrugInteraction>()

            // Search drug1's label for mentions of drug2
            val drug1Interactions = searchDrugLabelForInteraction(drug1, drug2)
            interactions.addAll(drug1Interactions)

            // Search drug2's label for mentions of drug1
            val drug2Interactions = searchDrugLabelForInteraction(drug2, drug1)
            interactions.addAll(drug2Interactions)

            // Deduplicate by normalizing the pair order
            val deduplicated = interactions
                .distinctBy {
                    val names = listOf(
                        it.medicineName1.lowercase(),
                        it.medicineName2.lowercase()
                    ).sorted()
                    "${names[0]}|${names[1]}|${it.description.take(100)}"
                }

            Result.Success(deduplicated)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to check drug interaction",
                throwable = e
            )
        }
    }

    override suspend fun getDrugInfo(drugName: String): Result<DrugLabelResult?> {
        return try {
            val searchQuery = "openfda.generic_name:\"$drugName\"+openfda.brand_name:\"$drugName\""
            val response = openFdaApi.searchDrugLabel(search = searchQuery, limit = 1)

            if (response.isSuccessful) {
                val result = response.body()?.results?.firstOrNull()
                Result.Success(result)
            } else {
                // 404 means no results found — not an error for the user
                if (response.code() == 404) {
                    Result.Success(null)
                } else {
                    Result.Error("OpenFDA API error: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to fetch drug info",
                throwable = e
            )
        }
    }

    override suspend fun checkAllInteractions(
        medicines: List<String>
    ): Result<List<DrugInteraction>> {
        if (medicines.size < 2) {
            return Result.Success(emptyList())
        }

        return try {
            val allInteractions = mutableListOf<DrugInteraction>()

            // Generate all unique pairs
            for (i in medicines.indices) {
                for (j in i + 1 until medicines.size) {
                    val result = checkInteraction(medicines[i], medicines[j])
                    if (result is Result.Success) {
                        allInteractions.addAll(result.data)
                    }
                }
            }

            // Final deduplication across all pairs
            val deduplicated = allInteractions
                .distinctBy {
                    val names = listOf(
                        it.medicineName1.lowercase(),
                        it.medicineName2.lowercase()
                    ).sorted()
                    "${names[0]}|${names[1]}|${it.description.take(100)}"
                }

            Result.Success(deduplicated)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to check all interactions",
                throwable = e
            )
        }
    }

    private suspend fun searchDrugLabelForInteraction(
        sourceDrug: String,
        targetDrug: String
    ): List<DrugInteraction> {
        val interactions = mutableListOf<DrugInteraction>()

        try {
            val searchQuery = "openfda.generic_name:\"$sourceDrug\"+openfda.brand_name:\"$sourceDrug\""
            val response = openFdaApi.searchDrugLabel(search = searchQuery, limit = 5)

            if (!response.isSuccessful || response.body() == null) {
                return emptyList()
            }

            val results = response.body()?.results ?: return emptyList()

            for (labelResult in results) {
                // Check drug_interactions field
                labelResult.drug_interactions?.forEach { interactionText ->
                    if (interactionText.contains(targetDrug, ignoreCase = true)) {
                        val severity = parseSeverity(interactionText)
                        val description = extractRelevantSection(interactionText, targetDrug)
                        interactions.add(
                            DrugInteraction(
                                medicineName1 = sourceDrug,
                                medicineName2 = targetDrug,
                                severity = severity,
                                description = description,
                                source = "OpenFDA Drug Label"
                            )
                        )
                    }
                }

                // Check warnings field for interaction mentions
                labelResult.warnings?.forEach { warningText ->
                    if (warningText.contains(targetDrug, ignoreCase = true) &&
                        containsInteractionKeywords(warningText)
                    ) {
                        val severity = parseSeverity(warningText)
                        val description = extractRelevantSection(warningText, targetDrug)
                        interactions.add(
                            DrugInteraction(
                                medicineName1 = sourceDrug,
                                medicineName2 = targetDrug,
                                severity = severity,
                                description = description,
                                source = "OpenFDA Drug Label (Warnings)"
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Silently handle individual search failures; caller handles aggregated errors
        }

        return interactions
    }

    private fun parseSeverity(text: String): InteractionSeverity {
        val lowerText = text.lowercase()
        return when {
            lowerText.contains("severe") || lowerText.contains("serious") ||
                lowerText.contains("life-threatening") || lowerText.contains("fatal") ||
                lowerText.contains("contraindicated") -> InteractionSeverity.SEVERE

            lowerText.contains("moderate") || lowerText.contains("caution") ||
                lowerText.contains("significant") -> InteractionSeverity.MODERATE

            lowerText.contains("mild") || lowerText.contains("minor") ||
                lowerText.contains("slight") -> InteractionSeverity.MILD

            else -> InteractionSeverity.UNKNOWN
        }
    }

    private fun extractRelevantSection(text: String, drugName: String): String {
        val lowerText = text.lowercase()
        val drugIndex = lowerText.indexOf(drugName.lowercase())

        if (drugIndex == -1) return text.take(MAX_DESCRIPTION_LENGTH)

        // Extract a window around the mention of the target drug
        val start = maxOf(0, drugIndex - CONTEXT_WINDOW)
        val end = minOf(text.length, drugIndex + drugName.length + CONTEXT_WINDOW)

        val extracted = text.substring(start, end).trim()

        // Clean up: start from the beginning of a sentence
        val sentenceStart = extracted.indexOfFirst { it == '.' || it == ';' }
        val cleanStart = if (sentenceStart in 0 until extracted.length / 3) {
            extracted.substring(sentenceStart + 1).trim()
        } else {
            extracted
        }

        return if (cleanStart.length > MAX_DESCRIPTION_LENGTH) {
            cleanStart.take(MAX_DESCRIPTION_LENGTH) + "…"
        } else {
            cleanStart
        }
    }

    private fun containsInteractionKeywords(text: String): Boolean {
        val keywords = listOf(
            "interaction", "interact", "concomitant", "co-administer",
            "coadminister", "concurrent", "combination", "together with",
            "when used with", "taken with", "combined with"
        )
        val lowerText = text.lowercase()
        return keywords.any { lowerText.contains(it) }
    }

    companion object {
        private const val CONTEXT_WINDOW = 200
        private const val MAX_DESCRIPTION_LENGTH = 500
    }
}
