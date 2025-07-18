package com.example.cartrack.features.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cartrack.core.data.model.notification.NotificationResponseDto
import com.example.cartrack.core.domain.repository.NotificationRepository
import com.example.cartrack.core.storage.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    private val logTag = "NotificationsVM"

    init {
        fetchNotifications()
    }

    fun refreshNotifications() {
        fetchNotifications(isRetry = true)
    }

    private fun fetchNotifications(isRetry: Boolean = false) {
        if (!isRetry) _uiState.update { it.copy(isLoading = true, error = null) }
        else _uiState.update { it.copy(isLoading = false, error = null) }

        viewModelScope.launch {
            notificationRepository.getNotifications().onSuccess { notifications ->
                markAsReadOnServer(notifications)
                userManager.setHasNewNotifications(false)
                _uiState.update { it.copy(isLoading = false, groupedNotifications = groupNotificationsByTime(notifications)) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun markAsReadOnServer(notifications: List<NotificationResponseDto>) {
        val unreadIds = notifications.filter { !it.isRead }.map { it.id }
        if (unreadIds.isNotEmpty()) {
            viewModelScope.launch {
                notificationRepository.markNotificationsAsRead(unreadIds).onFailure { e ->
                    Log.e(logTag, "Failed to mark notifications as read: ${e.message}")
                }
            }
        }
    }

    private fun groupNotificationsByTime(notifications: List<NotificationResponseDto>): Map<TimeCategory, List<NotificationResponseDto>> {
        val systemTimeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(systemTimeZone)
        val groupedByPairs = notifications
            .mapNotNull { notif -> try { Pair(notif, Instant.parse(notif.date).toLocalDateTime(systemTimeZone)) } catch (e: Exception) { null } }
            .sortedByDescending { it.second }
            .groupBy { getCategoryForDate(it.second.date, today) }
        return groupedByPairs.mapValues { entry -> entry.value.map { pair -> pair.first } }
    }

    private fun getCategoryForDate(notificationDate: LocalDate, today: LocalDate): TimeCategory {
        val daysDifference = today.until(notificationDate, DateTimeUnit.DAY)
        return when (daysDifference) {
            0 -> TimeCategory.TODAY
            -1 -> TimeCategory.YESTERDAY
            in -6..-2 -> TimeCategory.THIS_WEEK
            else -> TimeCategory.OLDER
        }
    }
}