package com.project.zariya.core.navigation

sealed class Route(val route: String) {
    object Auth : Route("auth")
    object Onboarding : Route("onboarding")
    object Home : Route("home")
    object MedicineList : Route("medicine_list")
    object AddEditMedicine : Route("add_edit_medicine?id={id}") {
        fun createRoute(id: String?) = "add_edit_medicine?id=${id ?: ""}"
    }
    object MedicineDetail : Route("medicine_detail/{id}") {
        fun createRoute(id: String) = "medicine_detail/$id"
    }
    object ReminderList : Route("reminder_list")
    object AddEditReminder : Route("add_edit_reminder?id={id}&medicineId={medicineId}") {
        fun createRoute(id: String?, medicineId: String? = null): String {
            val base = "add_edit_reminder?id=${id ?: ""}"
            return if (medicineId != null) "$base&medicineId=$medicineId" else base
        }
    }
    object RefillList : Route("refill_list")
    object Scanner : Route("scanner")
    object ScanResult : Route("scan_result")
    object DrugInteractions : Route("drug_interactions")
    object HealthDashboard : Route("health_dashboard")
    object ProfileList : Route("profile_list")
    object AddEditProfile : Route("add_edit_profile?id={id}") {
        fun createRoute(id: String?) = "add_edit_profile?id=${id ?: ""}"
    }
    object Analytics : Route("analytics")
    object InventoryHealth : Route("inventory_health")
    object ForHer : Route("for_her")
    object ForHerLuna : Route("for_her_luna")
}
