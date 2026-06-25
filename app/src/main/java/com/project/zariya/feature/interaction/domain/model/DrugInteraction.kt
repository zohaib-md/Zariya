package com.project.zariya.feature.interaction.domain.model

data class DrugInteraction(
    val medicineName1: String,
    val medicineName2: String,
    val severity: InteractionSeverity,
    val description: String,
    val source: String
)

enum class InteractionSeverity(
    val displayName: String,
    val colorDescription: String
) {
    MILD(
        displayName = "Mild",
        colorDescription = "Green"
    ),
    MODERATE(
        displayName = "Moderate",
        colorDescription = "Amber"
    ),
    SEVERE(
        displayName = "Severe",
        colorDescription = "Red"
    ),
    UNKNOWN(
        displayName = "Unknown",
        colorDescription = "Gray"
    )
}
