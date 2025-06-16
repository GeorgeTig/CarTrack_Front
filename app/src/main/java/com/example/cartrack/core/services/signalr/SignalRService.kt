package com.example.cartrack.core.services.signalr

import android.util.Log
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRService @Inject constructor(
    private val userManager: UserManager,
    private val tokenManager: TokenManager
) {
    @Volatile private var hubConnection: HubConnection? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Volatile private var connectionJob: Job? = null

    private val baseHubUrl = "http://10.0.2.2:5098/reminderHub"

    companion object {
        private const val TAG = "SignalRService"
        private const val RECONNECT_DELAY_MS = 5000L
    }

    fun startConnection() {
        Log.d(TAG, "startConnection called. Job active: ${connectionJob?.isActive}, Hub state: ${hubConnection?.connectionState}")

        if (connectionJob?.isActive == true) {
            Log.d(TAG, "Connection attempt already in progress or connection is active.")
            return
        }
        connectionJob?.cancel()

        connectionJob = serviceScope.launch {
            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.w(TAG, "Cannot start SignalR connection: Access Token is missing.")
                return@launch
            }

            val accessTokenProvider = Single.defer { Single.just(token) }

            val localHubConnection = HubConnectionBuilder.create(baseHubUrl)
                .withAccessTokenProvider(accessTokenProvider)
                .build()

            hubConnection = localHubConnection

            localHubConnection.on("UpdateReminders") {
                Log.i(TAG, "Received 'UpdateReminders' event from SignalR Hub.")
                if (serviceScope.isActive) {
                    launch { userManager.setHasNewNotifications(true) }
                }
            }

            localHubConnection.onClosed { exception ->
                Log.e(TAG, "SignalR connection closed.", exception)
                if (serviceScope.isActive && connectionJob?.isCancelled == false) {
                    attemptReconnect()
                }
            }

            try {
                (localHubConnection.start() as Completable).await()
                if (localHubConnection.connectionState == HubConnectionState.CONNECTED) {
                    Log.i(TAG, "SignalR Connection successful! ID: ${localHubConnection.connectionId}")
                } else {
                    Log.e(TAG, "SignalR Connection failed to start. State: ${localHubConnection.connectionState}")
                    if (serviceScope.isActive) attemptReconnect()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during SignalR connection start: ${e.message}", e)
                if (serviceScope.isActive) attemptReconnect()
            }
        }
    }

    private fun attemptReconnect() {
        serviceScope.launch {
            Log.d(TAG, "Attempting to reconnect in ${RECONNECT_DELAY_MS / 1000} seconds...")
            delay(RECONNECT_DELAY_MS)
            if (this.isActive) {
                hubConnection?.let {
                    if (it.connectionState != HubConnectionState.DISCONNECTED) {
                        try { (it.stop() as Completable).await() }
                        catch (e: Exception) { Log.w(TAG, "Exception during stop in reconnect: ${e.message}") }
                    }
                }
                hubConnection = null
                startConnection()
            }
        }
    }

    fun stopConnection() {
        Log.d(TAG, "stopConnection called.")
        connectionJob?.cancel()
        connectionJob = null

        val currentHub = hubConnection
        if (currentHub != null) {
            serviceScope.launch {
                try {
                    if (currentHub.connectionState != HubConnectionState.DISCONNECTED) {
                        (currentHub.stop() as Completable).await()
                        Log.i(TAG, "SignalR connection stopped successfully.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping SignalR connection: ${e.message}", e)
                }
            }
        }
        hubConnection = null
    }
}