package com.project.zariya.feature.home.domain

import com.project.zariya.feature.home.domain.model.HomeInsight
import com.project.zariya.feature.home.domain.model.InsightPriority
import com.project.zariya.feature.refill.domain.model.RefillInfo
import com.project.zariya.feature.reminder.domain.model.DoseLog
import com.project.zariya.feature.reminder.domain.model.DoseStatus
import com.project.zariya.core.util.DateTimeUtils
import javax.inject.Inject

class InsightEngine @Inject constructor() {

    fun generateInsight(
        todayLogs: List<DoseLog>,
        lowStockItems: List<RefillInfo>,
        weeklyAdherencePercentage: Int // We'll pass this calculated value from ViewModel
    ): HomeInsight {

        // 1. Critical Priority: Missed Medications Today
        val missedToday = todayLogs.filter { it.status == DoseStatus.SKIPPED || it.status == DoseStatus.MISSED }
        if (missedToday.isNotEmpty()) {
            val names = missedToday.map { it.medicineName }.distinct()
            val text = if (names.size == 1) "${names.first()} was missed today." else "${names.size} medications were missed today."
            return HomeInsight(
                title = "Missed Medication",
                message = text,
                priority = InsightPriority.CRITICAL
            )
        }

        // 2. Critical/High Priority: Empty or Critically Low Inventory
        if (lowStockItems.isNotEmpty()) {
            val emptyItems = lowStockItems.filter { it.currentStock <= 0 }
            if (emptyItems.isNotEmpty()) {
                val names = emptyItems.map { it.medicineName }
                val text = if (names.size == 1) "${names.first()} is completely out of stock." else "${names.size} medicines are out of stock."
                return HomeInsight(
                    title = "Out of Stock",
                    message = text,
                    priority = InsightPriority.CRITICAL
                )
            }
            
            // Just low stock
            val names = lowStockItems.map { it.medicineName }
            val text = if (names.size == 1) "${names.first()} may run out soon." else "${names.size} medicines require refill this week."
            return HomeInsight(
                title = "Refill Needed",
                message = text,
                priority = InsightPriority.HIGH
            )
        }

        // 3. Success Priority: All Caught Up Today
        val takenToday = todayLogs.filter { it.status == DoseStatus.TAKEN }
        if (todayLogs.isNotEmpty() && takenToday.size == todayLogs.size) {
            return HomeInsight(
                title = "All Caught Up!",
                message = "You've completed all medications for today. Great job!",
                priority = InsightPriority.HIGH
            )
        }

        // 4. Motivational: Weekly Adherence Trend
        if (weeklyAdherencePercentage >= 90) {
            return HomeInsight(
                title = "Excellent Consistency",
                message = "You have $weeklyAdherencePercentage% adherence this week. Keep up the streak!",
                priority = InsightPriority.MEDIUM
            )
        } else if (weeklyAdherencePercentage in 1..89) {
            return HomeInsight(
                title = "Weekly Progress",
                message = "Your adherence is $weeklyAdherencePercentage% this week. Let's aim higher tomorrow!",
                priority = InsightPriority.MEDIUM
            )
        }

        // 5. Informational: Next Dose or Remaining Doses
        val pendingToday = todayLogs.filter { it.status == DoseStatus.PENDING }
        if (pendingToday.isNotEmpty()) {
            val nextDose = pendingToday.minByOrNull { it.scheduledTime }
            if (nextDose != null) {
                val timeStr = DateTimeUtils.run { nextDose.scheduledTime.formatTime() }
                return HomeInsight(
                    title = "Next Dose Coming Up",
                    message = "${nextDose.medicineName} is scheduled for $timeStr.",
                    priority = InsightPriority.LOW
                )
            }
        }

        // Fallback
        return HomeInsight(
            title = "Inventory Healthy",
            message = "Your inventory is fully stocked.",
            priority = InsightPriority.LOW
        )
    }
}
