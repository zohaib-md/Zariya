package com.project.zariya.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateTimeUtils {

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd MMM")
    private val dayFormatter = DateTimeFormatter.ofPattern("EEEE")

    fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

    fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

    fun LocalDateTime.toEpochMillis(): Long =
        this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun LocalDate.toEpochMillis(): Long =
        this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun LocalTime.toEpochMillisToday(): Long {
        val today = LocalDate.now()
        return LocalDateTime.of(today, this).toEpochMillis()
    }

    fun Long.formatTime(): String =
        this.toLocalDateTime().format(timeFormatter)

    fun Long.formatDate(): String =
        this.toLocalDateTime().format(dateFormatter)

    fun Long.formatDateTime(): String =
        this.toLocalDateTime().format(dateTimeFormatter)

    fun Long.formatShortDate(): String =
        this.toLocalDateTime().format(shortDateFormatter)

    fun Long.formatDayOfWeek(): String =
        this.toLocalDateTime().format(dayFormatter)

    fun LocalTime.formatTime(): String =
        this.format(timeFormatter)

    fun nowMillis(): Long = System.currentTimeMillis()

    fun todayStartMillis(): Long =
        LocalDate.now().toEpochMillis()

    fun todayEndMillis(): Long =
        LocalDate.now().plusDays(1).toEpochMillis() - 1

    fun daysUntil(futureMillis: Long): Long {
        val now = LocalDate.now()
        val future = futureMillis.toLocalDate()
        return ChronoUnit.DAYS.between(now, future)
    }

    fun minutesUntil(futureMillis: Long): Long {
        val now = Instant.now()
        val future = Instant.ofEpochMilli(futureMillis)
        return ChronoUnit.MINUTES.between(now, future)
    }

    fun formatCountdown(targetMillis: Long): String {
        val diffMillis = targetMillis - System.currentTimeMillis()
        if (diffMillis <= 0) return "Now"

        val hours = diffMillis / (1000 * 60 * 60)
        val minutes = (diffMillis / (1000 * 60)) % 60

        return when {
            hours > 24 -> "${hours / 24}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }

    fun getStartOfWeek(): Long {
        val now = LocalDate.now()
        val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
        return startOfWeek.toEpochMillis()
    }

    fun getStartOfMonth(): Long {
        val now = LocalDate.now()
        return now.withDayOfMonth(1).toEpochMillis()
    }
}
