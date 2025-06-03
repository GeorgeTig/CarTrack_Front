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
        markNotificationsAsRead()
        fetchNotifications()
    }

    private fun markNotificationsAsRead() {
        viewModelScope.launch {
            userManager.setHasNewNotifications(false)
            Log.d(logTag, "Notification indicator 'hasNewNotifications' set to false.")
        }
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
                val grouped = groupNotificationsByTime(fetchedNotifications) // Am redenumit înapoi la groupNotificationsByTime
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

    private fun parseNotificationDate(dateString: String): LocalDateTime? {
        return try {
            // kotlinx.datetime parsează direct string-uri ISO 8601
            Instant.parse(dateString).toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (e: Exception) { // Prinde orice excepție de la Instant.parse (IllegalArgumentException etc.)
            Log.e(logTag, "Failed to parse date string '$dateString' with Instant.parse: ${e.message}")
            // Nu mai adăugăm un fallback complex aici. Backend-ul AR TREBUI să trimită un format consistent.
            // Dacă backend-ul trimite un format total diferit, cum ar fi "yyyy-MM-dd HH:mm:ss",
            // atunci ar trebui folosit java.time.LocalDateTime.parse cu un DateTimeFormatter specific AICI,
            // și apoi convertit la kotlinx.datetime.LocalDateTime.
            // Exemplu (dacă backend trimite "2023-10-27 10:30:00"):
            /*
            try {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val javaLdt = java.time.LocalDateTime.parse(dateString, formatter)
                return LocalDateTime(javaLdt.year, javaLdt.monthValue, javaLdt.dayOfMonth, javaLdt.hour, javaLdt.minute, javaLdt.second)
            } catch (e2: DateTimeParseException) {
                Log.e(logTag, "Failed to parse date string '$dateString' with java.time fallback: ${e2.message}")
                return null
            }
            */
            return null // Returnează null dacă parsarea ISO eșuează
        }
    }

    private fun getCategoryForDate(notificationLocalDateTime: LocalDateTime, today: LocalDate): TimeCategory {
        val notificationDate = notificationLocalDateTime.date
        val daysDifference = today.toEpochDays() - notificationDate.toEpochDays()

        return when {
            daysDifference.toLong() == 0L -> TimeCategory.TODAY // Este azi
            daysDifference.toLong() == 1L -> TimeCategory.YESTERDAY // Este ieri
            daysDifference > 1L && daysDifference < 7L -> TimeCategory.THIS_WEEK // Între 2 și 6 zile în urmă
            daysDifference >= 7L && daysDifference < 30L -> TimeCategory.THIS_MONTH // Între 7 și 29 zile în urmă
            else -> TimeCategory.OLDER // Mai vechi de 30 de zile (sau în viitor, deși nu ar trebui)
        }
    }

    private fun groupNotificationsByTime(notifications: List<NotificationResponseDto>): Map<TimeCategory, List<NotificationResponseDto>> {
        val systemTimeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(systemTimeZone) // Obține LocalDate pentru azi

        val grouped = mutableMapOf<TimeCategory, MutableList<NotificationResponseDto>>()

        val sortedNotifications = notifications.mapNotNull { notification ->
            parseNotificationDate(notification.date)?.let { dateTime ->
                Pair(notification, dateTime)
            }
        }.sortedByDescending { it.second } // Sortează descrescător după LocalDateTime

        for ((notification, dateTime) in sortedNotifications) {
            val category = getCategoryForDate(dateTime, today)
            grouped.getOrPut(category) { mutableListOf() }.add(notification)
        }

        // Asigură ordinea dorită a categoriilor în Map (Enum.values() păstrează ordinea declarării)
        val orderedGroupedNotifications = linkedMapOf<TimeCategory, List<NotificationResponseDto>>()
        TimeCategory.values().forEach { category -> // Iterează prin valorile enum-ului în ordinea definirii lor
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