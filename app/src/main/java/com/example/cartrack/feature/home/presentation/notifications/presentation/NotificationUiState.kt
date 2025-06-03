package com.example.cartrack.feature.home.presentation.notifications.presentation

import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto

// Definește categoriile de timp
enum class TimeCategory(val displayName: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    OLDER("Older")
}

data class NotificationsUiState(
    val isLoading: Boolean = false,
    // Stochează notificările grupate
    val groupedNotifications: Map<TimeCategory, List<NotificationResponseDto>> = emptyMap(),
    // Păstrează și lista originală dacă e utilă pentru alte scopuri, deși probabil nu
    // val rawNotifications: List<NotificationResponseDto> = emptyList(),
    val error: String? = null
)