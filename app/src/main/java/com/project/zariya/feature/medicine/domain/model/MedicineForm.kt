package com.project.zariya.feature.medicine.domain.model

enum class MedicineForm(val displayName: String) {
    TABLET("Tablet"),
    CAPSULE("Capsule"),
    SYRUP("Syrup"),
    INJECTION("Injection"),
    DROPS("Drops"),
    INHALER("Inhaler"),
    CREAM("Cream/Ointment"),
    PATCH("Patch"),
    OTHER("Other")
}
