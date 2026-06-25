package com.project.zariya.feature.analytics.domain.usecase

import com.project.zariya.feature.analytics.domain.model.AdherenceStats
import com.project.zariya.feature.analytics.domain.model.DailyAdherence
import com.project.zariya.feature.analytics.domain.model.WeeklyReport
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.feature.reminder.domain.usecase.GetDailyDosesUseCase
import com.project.zariya.core.util.DateTimeUtils
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetAdherenceStatsUseCase @Inject constructor(
    private val getDailyDosesUseCase: GetDailyDosesUseCase
) {
    suspend fun getToday(profileId: String): AdherenceStats {
        val startOfDay = DateTimeUtils.todayStartMillis()
        return buildStats(profileId, startOfDay)
    }

    suspend fun getWeekly(profileId: String): WeeklyReport {
        val weekStart = DateTimeUtils.getStartOfWeek()
        val weekEnd = DateTimeUtils.todayEndMillis()

        val dailyStats = mutableListOf<DailyAdherence>()
        val startDate = java.time.Instant.ofEpochMilli(weekStart)
            .atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()

        var current = startDate
        while (!current.isAfter(today)) {
            val dayStart = current.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val dailyDoses = getDailyDosesUseCase(profileId, dayStart).first()
            val taken = dailyDoses.count { it.status == DoseStatus.TAKEN }
            val total = dailyDoses.size

            dailyStats.add(
                DailyAdherence(
                    date = dayStart,
                    taken = taken,
                    total = if (total > 0) total else 1,
                    percentage = if (total > 0) taken.toFloat() / total.toFloat() * 100f else 0f
                )
            )
            current = current.plusDays(1)
        }

        val overallTaken = dailyStats.sumOf { it.taken }
        val overallTotal = dailyStats.sumOf { if (it.total == 1 && it.taken == 0) 0 else it.total } // Adjust to actual total
        val adjustedTotal = if (overallTotal == 0) 0 else dailyStats.sumOf { it.total }

        return WeeklyReport(
            weekStart = weekStart,
            weekEnd = weekEnd,
            dailyStats = dailyStats,
            overallPercentage = if (adjustedTotal > 0) overallTaken.toFloat() / adjustedTotal.toFloat() * 100f else 0f
        )
    }

    suspend fun getMonthly(profileId: String): List<DailyAdherence> {
        val monthStart = DateTimeUtils.getStartOfMonth()
        val today = LocalDate.now()
        val startDate = java.time.Instant.ofEpochMilli(monthStart)
            .atZone(ZoneId.systemDefault()).toLocalDate()

        val dailyStats = mutableListOf<DailyAdherence>()
        var current = startDate
        while (!current.isAfter(today)) {
            val dayStart = current.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val dailyDoses = getDailyDosesUseCase(profileId, dayStart).first()
            val taken = dailyDoses.count { it.status == DoseStatus.TAKEN }
            val total = dailyDoses.size

            dailyStats.add(
                DailyAdherence(
                    date = dayStart,
                    taken = taken,
                    total = if (total > 0) total else 1,
                    percentage = if (total > 0) taken.toFloat() / total.toFloat() * 100f else 0f
                )
            )
            current = current.plusDays(1)
        }
        return dailyStats
    }

    private suspend fun buildStats(profileId: String, dateMillis: Long): AdherenceStats {
        val dailyDoses = getDailyDosesUseCase(profileId, dateMillis).first()
        val taken = dailyDoses.count { it.status == DoseStatus.TAKEN }
        val missed = dailyDoses.count { it.status == DoseStatus.MISSED }
        val skipped = dailyDoses.count { it.status == DoseStatus.SKIPPED }
        val total = dailyDoses.size

        return AdherenceStats(
            totalDoses = total,
            takenDoses = taken,
            missedDoses = missed,
            skippedDoses = skipped,
            adherencePercentage = if (total > 0) taken.toFloat() / total.toFloat() * 100f else 0f,
            streakDays = 0 // To be implemented separately
        )
    }
}

