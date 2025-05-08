package com.example.cartrack.core.ui.cards.ReminderCard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// --- Enum for Maintenance Category Icons ---
enum class MaintenanceCategoryIcon(val icon: ImageVector) {
    ENGINE(Icons.Filled.Settings),
    WHEELS(Icons.Filled.Build),
    BRAKES(Icons.Filled.Build),
    FLUIDS(Icons.Filled.Build),
    BATTERY(Icons.Filled.Build),
    INSPECTION(Icons.Filled.Search),
    FILTERS(Icons.Filled.Build),
    LIGHTS(Icons.Filled.Build),
    OTHER(Icons.Filled.Build);

    companion object {

        fun fromCategoryName(categoryName: String?): MaintenanceCategoryIcon {
            return when (categoryName?.lowercase()?.trim()) {
                "engine" -> ENGINE
                "wheels", "tires" -> WHEELS
                "brakes" -> BRAKES
                "fluids", "oil" -> FLUIDS
                "battery", "electrical" -> BATTERY
                "inspection" -> INSPECTION
                "filters" -> FILTERS
                "lights" -> LIGHTS

                else -> OTHER
            }
        }
    }
}