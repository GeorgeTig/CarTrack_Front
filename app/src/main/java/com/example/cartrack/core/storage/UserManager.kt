package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow

interface UserManager {
    /**
     * A flow that emits true if there are new unread notifications, false otherwise.
     */
    val hasNewNotificationsFlow: Flow<Boolean>

    /**
     * Sets the status of new notifications.
     * @param hasNew true if there are new notifications, false otherwise.
     */
    suspend fun setHasNewNotifications(hasNew: Boolean)

    /**
     * Saves the client ID.
     */
    suspend fun saveClientId(clientId: Int)

    /**
     * Retrieves the client ID as a Flow. Emits null if no ID is cached.
     */
    val clientIdFlow: Flow<Int?>

    /**
     * Deletes all user-specific cached data (client ID, notification status).
     */
    suspend fun clearUserData()
}