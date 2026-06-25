package com.project.zariya.feature.reminder.domain.model

enum class ScheduleType(val displayName: String) {
    DAILY("Daily"),
    EVERY_N_HOURS("Every N Hours"),
    SPECIFIC_DAYS("Specific Days"),
    CYCLIC("Cyclic")
}
