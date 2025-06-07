package com.example.cartrack.features.notifications

import com.example.cartrack.core.data.model.notification.NotificationResponseDto

enum class TimeCategory(val displayName: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    OLDER("Older")
}

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val groupedNotifications: Map<TimeCategory, List<NotificationResponseDto>> = emptyMap(),
    val error: String? = null
)