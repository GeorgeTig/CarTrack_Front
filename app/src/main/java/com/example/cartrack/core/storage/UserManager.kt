package com.example.cartrack.core.storage

import kotlinx.coroutines.flow.Flow

interface UserManager {
    suspend fun saveClientId(clientId: Int)
    suspend fun setHasNewNotifications(hasNew: Boolean)
    suspend fun clearUserData()

    val clientIdFlow: Flow<Int?>
    val hasNewNotificationsFlow: Flow<Boolean>
}