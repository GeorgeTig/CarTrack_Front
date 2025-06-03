package com.example.cartrack.core.signalr

import android.util.Log
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Completable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

        if (connectionJob?.isActive == true && (hubConnection?.connectionState == HubConnectionState.CONNECTING || hubConnection?.connectionState == HubConnectionState.CONNECTED)) {
            Log.d(TAG, "Connection attempt already in progress or connection is active.")
            return
        }
        connectionJob?.cancel()

        connectionJob = serviceScope.launch {
            Log.d(TAG, "Coroutine for startConnection launched. Scope active: $isActive")

            val token = tokenManager.accessTokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.w(TAG, "Cannot start SignalR connection: Access Token is missing.")
                return@launch
            }

            val hubUrlWithToken = "$baseHubUrl?access_token=$token"
            Log.d(TAG, "Attempting to start SignalR connection to $hubUrlWithToken")

            val localHubConnection = HubConnectionBuilder.create(hubUrlWithToken).build()
            hubConnection = localHubConnection

            localHubConnection.on("UpdateReminders", {
                Log.i(TAG, "Received 'UpdateReminders' event from SignalR Hub.")
                serviceScope.launch {
                    if (this.isActive) {
                        userManager.setHasNewNotifications(true)
                        Log.d(TAG, "Set 'hasNewNotifications' to true via UserManager.")
                    }
                }
            })

            localHubConnection.onClosed { exception ->
                Log.e(TAG, "SignalR connection closed.", exception)
                if (serviceScope.isActive && connectionJob?.isCancelled == false) {
                    attemptReconnect()
                }
            }

            try {
                Log.d(TAG, "Before localHubConnection.start()")
                val startOperation = localHubConnection.start()
                // Log.d(TAG, "Type of result from localHubConnection.start(): ${startOperation::class.java.name}") // Poate fi decomentat pentru debug
                (startOperation as Completable).await()
                Log.d(TAG, "After localHubConnection.start()")

                if (localHubConnection.connectionState == HubConnectionState.CONNECTED) {
                    Log.i(TAG, "SignalR Connection successful! Connection ID: ${localHubConnection.connectionId}")
                } else {
                    Log.e(TAG, "SignalR Connection failed to start. State: ${localHubConnection.connectionState}")
                    if (serviceScope.isActive && connectionJob?.isCancelled == false) {
                        attemptReconnect()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during SignalR connection (in try-catch for start): ${e.message}", e)
                if (serviceScope.isActive && connectionJob?.isCancelled == false) {
                    attemptReconnect()
                }
            }
        }
        Log.d(TAG, "startConnection finished scheduling coroutine.")
    }

    private fun attemptReconnect() {
        Log.d(TAG, "Scheduling reconnect attempt...")
        serviceScope.launch {
            Log.d(TAG, "Attempting to reconnect in ${RECONNECT_DELAY_MS / 1000} seconds...")
            delay(RECONNECT_DELAY_MS)
            if (this.isActive) {
                Log.d(TAG, "Reconnect coroutine active, proceeding with stop and start.")
                val currentHub = hubConnection
                if (currentHub != null && currentHub.connectionState != HubConnectionState.DISCONNECTED) {
                    try {
                        Log.d(TAG, "Attempting to stop connection in reconnect... State: ${currentHub.connectionState}")
                        (currentHub.stop() as Completable).await()
                        Log.d(TAG, "Connection stopped in reconnect.")
                    } catch (e: Exception) {
                        Log.w(TAG, "Exception during stop in reconnect: ${e.message}")
                    }
                }
                hubConnection = null
                startConnection()
            } else {
                Log.d(TAG, "Reconnect attempt aborted: Coroutine scope for reconnect is no longer active.")
            }
        }
    }

    fun stopConnection(cancelOuterJob: Boolean = true) {
        Log.d(TAG, "stopConnection called. cancelOuterJob: $cancelOuterJob")
        val jobToCancel = if (cancelOuterJob) connectionJob else null
        val currentHub = hubConnection

        serviceScope.launch {
            if (currentHub != null) {
                if (currentHub.connectionState == HubConnectionState.CONNECTED || currentHub.connectionState == HubConnectionState.CONNECTING) {
                    try {
                        Log.d(TAG, "Attempting to stop hub connection via stopConnection call...")
                        (currentHub.stop() as Completable).await()
                        Log.i(TAG, "SignalR connection stopped successfully via stopConnection call.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping SignalR connection via stopConnection call: ${e.message}", e)
                    }
                } else {
                    Log.d(TAG, "Hub connection already stopped or in an irrelevant state: ${currentHub.connectionState}. Not calling stop().")
                }
            }
            hubConnection = null

            if (jobToCancel != null && jobToCancel.isActive) {
                Log.d(TAG, "Cancelling connectionJob.")
                jobToCancel.cancel()
            }
            if (cancelOuterJob) {
                connectionJob = null
            }
        }
    }
}