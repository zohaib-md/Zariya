package com.project.zariya.feature.profile.domain.model

enum class ProfileType(val displayName: String) {
    SELF("Self"),
    PARENT("Parent"),
    CHILD("Child"),
    GRANDPARENT("Grandparent"),
    CUSTOM("Custom")
}
