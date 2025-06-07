package com.example.cartrack.navigation

object Routes {
    // --- Rute Publice ---
    const val SPLASH_LOADING = "splash_loading"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // --- Rute Protejate (după login) ---
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val EDIT_PROFILE = "edit_profile"
    const val NOTIFICATIONS = "notifications"
    const val ADD_MAINTENANCE = "add_maintenance"

    // --- Rute cu Argumente ---

    // ADD VEHICLE
    private const val ADD_VEHICLE_BASE = "add_vehicle"
    const val ADD_VEHICLE_ARG = "fromLoginNoVehicles" // <-- FĂCUT PUBLIC
    const val ADD_VEHICLE_ROUTE_DEF = "$ADD_VEHICLE_BASE?$ADD_VEHICLE_ARG={$ADD_VEHICLE_ARG}"
    fun addVehicleRoute(fromLoginNoVehicles: Boolean): String {
        return "$ADD_VEHICLE_BASE?$ADD_VEHICLE_ARG=$fromLoginNoVehicles"
    }

    // REMINDER
    private const val REMINDER_BASE = "reminder" // Am schimbat pentru a evita confuzia
    const val REMINDER_ARG_ID = "reminderId" // <-- FĂCUT PUBLIC

    // REMINDER DETAIL
    private const val REMINDER_DETAIL_BASE = "$REMINDER_BASE/detail"
    const val REMINDER_DETAIL_ROUTE_DEF = "$REMINDER_DETAIL_BASE/{$REMINDER_ARG_ID}"
    fun reminderDetailRoute(reminderId: Int): String {
        return "$REMINDER_DETAIL_BASE/$reminderId"
    }

    // EDIT REMINDER
    private const val EDIT_REMINDER_BASE = "$REMINDER_BASE/edit"
    const val EDIT_REMINDER_ROUTE_DEF = "$EDIT_REMINDER_BASE/{$REMINDER_ARG_ID}"
    fun editReminderRoute(reminderId: Int): String {
        return "$EDIT_REMINDER_BASE/$reminderId"
    }
}