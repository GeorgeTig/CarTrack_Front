package com.example.cartrack.navigation

object Routes {
    const val SPLASH_LOADING = "splash_loading"
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val EDIT_PROFILE = "edit_profile"
    const val NOTIFICATIONS = "notifications"
    const val ADD_MAINTENANCE = "add_maintenance"


    private const val ADD_VEHICLE_BASE = "add_vehicle"
    const val ADD_VEHICLE_ARG = "fromLoginNoVehicles"
    const val ADD_VEHICLE_ROUTE_DEF = "$ADD_VEHICLE_BASE?$ADD_VEHICLE_ARG={$ADD_VEHICLE_ARG}"
    fun addVehicleRoute(fromLoginNoVehicles: Boolean): String {
        return "$ADD_VEHICLE_BASE?$ADD_VEHICLE_ARG=$fromLoginNoVehicles"
    }

    private const val REMINDER_BASE = "reminder"
    const val REMINDER_ARG_ID = "reminderId"
    const val ADD_CUSTOM_REMINDER = "add_custom_reminder"

    private const val REMINDER_DETAIL_BASE = "$REMINDER_BASE/detail"
    const val REMINDER_DETAIL_ROUTE_DEF = "$REMINDER_DETAIL_BASE/{$REMINDER_ARG_ID}"
    fun reminderDetailRoute(reminderId: Int): String {
        return "$REMINDER_DETAIL_BASE/$reminderId"
    }

    private const val EDIT_REMINDER_BASE = "$REMINDER_BASE/edit"
    const val EDIT_REMINDER_ROUTE_DEF = "$EDIT_REMINDER_BASE/{$REMINDER_ARG_ID}"
    fun editReminderRoute(reminderId: Int): String {
        return "$EDIT_REMINDER_BASE/$reminderId"
    }

    const val CHANGE_PASSWORD = "change_password"

    const val CAR_HISTORY_BASE = "car_history"
    const val CAR_HISTORY_ARG_ID = "vehicleId"
    const val CAR_HISTORY_ROUTE_DEF = "$CAR_HISTORY_BASE/{$CAR_HISTORY_ARG_ID}"
    fun carHistoryRoute(vehicleId: Int) = "$CAR_HISTORY_BASE/$vehicleId"
}