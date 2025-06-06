package com.example.cartrack.feature.home.presentation.notifications.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto
import com.example.cartrack.feature.home.presentation.notifications.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod // Import necesar
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus // Import necesar
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.time.format.DateTimeParseException // Pentru gestionarea erorilor de parsare (dacă se folosește java.time ca fallback)
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val userManager: UserManager,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val logTag = "NotificationsVM"

    init {
        fetchNotifications()
    }

    fun fetchNotifications(isRetry: Boolean = false) {
        if (!isRetry) {
            _uiState.update { it.copy(isLoading = true, error = null, groupedNotifications = emptyMap()) }
        } else {
            _uiState.update { it.copy(error = null) }
        }
        Log.d(logTag, "Fetching notifications...")

        viewModelScope.launch {
            val clientId = userManager.clientIdFlow.firstOrNull()
            if (clientId == null) {
                Log.e(logTag, "Cannot fetch notifications: Client ID is null.")
                _uiState.update { it.copy(isLoading = false, error = "User not identified.") }
                return@launch
            }

            val result = notificationRepository.getNotificationsByClientId(clientId)
            result.onSuccess { fetchedNotifications ->
                Log.d(logTag, "Successfully fetched ${fetchedNotifications.size} notifications.")

                // --- AICI ESTE LOGICA CORECTATĂ ---
                // 1. Odată ce am primit notificările, le marcăm ca citite pe server.
                markAsReadOnServer(fetchedNotifications)
                // 2. Apoi, resetăm indicatorul local.
                userManager.setHasNewNotifications(false)
                // 3. La final, actualizăm UI-ul.

                val grouped = groupNotificationsByTime(fetchedNotifications)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groupedNotifications = grouped,
                        error = null
                    )
                }
            }.onFailure { exception ->
                Log.e(logTag, "Failed to fetch notifications: ${exception.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load notifications."
                    )
                }
            }
        }
    }

    private fun markAsReadOnServer(notifications: List<NotificationResponseDto>) {
        val unreadIds = notifications.filter { !it.isRead }.map { it.id }

        if (unreadIds.isEmpty()) {
            Log.d(logTag, "No unread notifications to mark on server.")
            return
        }

        viewModelScope.launch {
            Log.d(logTag, "Marking notification IDs as read on server: $unreadIds")
            notificationRepository.markNotificationsAsRead(unreadIds)
                .onFailure { e ->
                    Log.e(logTag, "Failed to mark notifications as read on server: ${e.message}")
                }
        }
    }

    private fun parseNotificationDate(dateString: String): LocalDateTime? {
        return try {
            Instant.parse(dateString).toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            Log.e(logTag, "Failed to parse date string '$dateString' with Instant.parse: ${e.message}")
            null
        }
    }

    private fun getCategoryForDate(notificationLocalDateTime: LocalDateTime, today: LocalDate): TimeCategory {
        val notificationDate = notificationLocalDateTime.date
        val daysDifference = today.toEpochDays() - notificationDate.toEpochDays()

        return when {
            daysDifference.toLong() == 0L -> TimeCategory.TODAY
            daysDifference.toLong() == 1L -> TimeCategory.YESTERDAY
            daysDifference in 2..6 -> TimeCategory.THIS_WEEK
            daysDifference in 7..29 -> TimeCategory.THIS_MONTH
            else -> TimeCategory.OLDER
        }
    }

    private fun groupNotificationsByTime(notifications: List<NotificationResponseDto>): Map<TimeCategory, List<NotificationResponseDto>> {
        val systemTimeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(systemTimeZone)

        val grouped = mutableMapOf<TimeCategory, MutableList<NotificationResponseDto>>()

        val sortedNotifications = notifications.mapNotNull { notification ->
            parseNotificationDate(notification.date)?.let { dateTime ->
                Pair(notification, dateTime)
            }
        }.sortedByDescending { it.second }

        for ((notification, dateTime) in sortedNotifications) {
            val category = getCategoryForDate(dateTime, today)
            grouped.getOrPut(category) { mutableListOf() }.add(notification)
        }

        val orderedGroupedNotifications = linkedMapOf<TimeCategory, List<NotificationResponseDto>>()
        TimeCategory.values().forEach { category ->
            grouped[category]?.let {
                orderedGroupedNotifications[category] = it
            }
        }
        return orderedGroupedNotifications
    }

    fun refreshNotifications() {
        Log.d(logTag, "Refresh notifications requested by UI.")
        fetchNotifications(isRetry = true)
    }
}