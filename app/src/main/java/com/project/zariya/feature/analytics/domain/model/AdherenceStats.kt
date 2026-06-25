package com.project.zariya.feature.analytics.domain.model

data class AdherenceStats(
    val totalDoses: Int,
    val takenDoses: Int,
    val missedDoses: Int,
    val skippedDoses: Int,
    val adherencePercentage: Float,
    val streakDays: Int = 0
)

data class DailyAdherence(
    val date: Long,
    val taken: Int,
    val total: Int,
    val percentage: Float
)

data class WeeklyReport(
    val weekStart: Long,
    val weekEnd: Long,
    val dailyStats: List<DailyAdherence>,
    val overallPercentage: Float
)

enum class Period(val displayName: String) {
    TODAY("Today"),
    WEEK("This Week"),
    MONTH("This Month")
}
