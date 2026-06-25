package com.project.zariya.feature.reminder.domain.model

enum class DoseStatus(val displayName: String) {
    TAKEN("Taken"),
    MISSED("Missed"),
    SKIPPED("Skipped"),
    SNOOZED("Snoozed"),
    PENDING("Pending")
}
