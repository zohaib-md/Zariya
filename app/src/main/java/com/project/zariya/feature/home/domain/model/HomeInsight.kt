package com.project.zariya.feature.home.domain.model

enum class InsightPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class HomeInsight(
    val title: String,
    val message: String,
    val priority: InsightPriority,
    val iconRes: Int? = null // Optional icon resource ID or a predefined type later
)
