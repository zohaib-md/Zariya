package com.project.zariya.feature.for_her.domain.util

import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class CyclePhase(val displayName: String, val message: String) {
    MENSTRUAL("Menstrual Phase", "Honor your energy. Rest, hydrate, and take it slow."),
    FOLLICULAR("Follicular Phase", "Your energy is rising. A great time for creativity and movement!"),
    OVULATION("Ovulation Phase", "Peak energy and confidence. You're glowing!"),
    LUTEAL("Luteal Phase", "Winding down. Focus on self-care and gentle routines."),
    UNKNOWN("Log Your Period", "Track your cycle to get personalized insights.")
}

object CyclePhaseCalculator {
    fun calculatePhase(startDateMillis: Long?, currentDateMillis: Long = System.currentTimeMillis()): CyclePhase {
        if (startDateMillis == null) return CyclePhase.UNKNOWN

        // Normalize dates to midnight to avoid time-of-day offsets
        val startCal = Calendar.getInstance().apply { 
            timeInMillis = startDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val currentCal = Calendar.getInstance().apply {
            timeInMillis = currentDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffInMillis = currentCal.timeInMillis - startCal.timeInMillis
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()

        // If the start date is in the future, or it's been more than a 28-day cycle, 
        // we map it to a recurring 28-day cycle for simplicity.
        val dayOfCycle = if (diffInDays >= 0) {
            (diffInDays % 28) + 1
        } else {
            // Negative diff means the date is in the future.
            // For simplicity, if they log a future date, we return unknown or map it.
            return CyclePhase.UNKNOWN
        }

        return when (dayOfCycle) {
            in 1..5 -> CyclePhase.MENSTRUAL
            in 6..14 -> CyclePhase.FOLLICULAR
            in 15..17 -> CyclePhase.OVULATION
            in 18..28 -> CyclePhase.LUTEAL
            else -> CyclePhase.UNKNOWN
        }
    }
}
