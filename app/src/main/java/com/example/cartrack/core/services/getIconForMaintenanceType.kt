package com.example.cartrack.core.services

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconForMaintenanceType(typeId: Int?): ImageVector {
    return when (typeId) {
        1 -> Icons.Filled.OilBarrel
        2 -> Icons.Filled.Opacity
        3 -> Icons.Filled.FilterAlt
        4 -> Icons.Filled.DiscFull
        5 -> Icons.Filled.ElectricalServices
        6 -> Icons.Filled.Compress
        7 -> Icons.Filled.Settings
        8 -> Icons.Filled.DirectionsCar
        9 -> Icons.Filled.AcUnit
        else -> Icons.Filled.Build
    }
}