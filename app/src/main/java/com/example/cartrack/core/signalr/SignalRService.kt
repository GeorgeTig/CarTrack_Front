package com.example.cartrack.core.signalr

import android.util.Log
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.UserManager
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob // Folosim SupervisorJob pentru a preveni anularea părinteului
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private suspend fun CompletableFuture<Void>.awaitFutureVoid() {
    if (this.isDone) {
        try {
            this.get()
            return
        } catch (e: Exception) {
            val cause = e.cause
            if (cause != null) throw cause
            throw e
        }
    }

    return suspendCancellableCoroutine { continuation ->
        this.whenComplete { _, exception ->
            if (exception != null) {
                val cause = exception.cause
                continuation.resumeWithException(cause ?: exception)
            } else {
                continuation.resume(Unit)
            }
        }
        continuation.invokeOnCancellation {
            this.cancel(false)
        }
    }
}


@Singleton
class SignalRService @Inject constructor(
    private val userManager: UserManager,
    private val tokenManager: TokenManager
) {
    @Volatile private var hubConnection: HubConnection? = null // Mark as volatile
    // Folosim SupervisorJob pentru ca eșecul unui copil (ex: reconectare) să nu anuleze întregul scope
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Volatile private var connectionJob: Job? = null // Mark as volatile

    private val baseHubUrl = "http://10.0.2.2:5098/reminderHub"

    companion object {
        private const val TAG = "SignalRService"
        private const val RECONNECT_DELAY_MS = 5000L
    }

    fun startConnection() {
        Log.d(TAG, "startConnection called. Current job active: ${connectionJob?.isActive}, connection state: ${hubConnection?.connectionState}")
        // Verificare mai robustă pentru a evita multiple încercări paralele
        if (connectionJob?.isActive == true && (hubConnection?.connectionState == HubConnectionState.CONNECTING || hubConnection?.connectionState == HubConnectionState.CONNECTED)) {
            Log.d(TAG, "Connection attempt already in progress or connection is active.")
            return
        }
        // Anulează un job anterior dacă există și nu mai este relevant
        connectionJob?.cancel()

        connectionJob = serviceScope.launch {
            Log.d(TAG, "Coroutine for startConnection launched. Scope active: $isActive")
            val token = tokenManager.tokenFlow.firstOrNull()
            if (token.isNullOrBlank()) {
                Log.w(TAG, "Cannot start SignalR connection: Token is missing.")
                // Nu mai setăm connectionJob la null aici, se va termina natural
                return@launch
            }

            val hubUrlWithToken = "$baseHubUrl?access_token=$token"
            Log.d(TAG, "Attempting to start SignalR connection to $hubUrlWithToken")

            val localHubConnection = HubConnectionBuilder.create(hubUrlWithToken).build()
            hubConnection = localHubConnection // Assign to class member

            localHubConnection.on("UpdateReminders", {
                Log.i(TAG, "Received 'UpdateReminders' event from SignalR Hub.")
                // Folosește serviceScope și pentru acest launch, pentru consistență
                serviceScope.launch {
                    if(this.isActive) { // Verifică dacă corutina încă e activă
                        userManager.setHasNewNotifications(true)
                        Log.d(TAG, "Set 'hasNewNotifications' to true via UserManager.")
                    }
                }
            }, /* No parameters expected */)

            localHubConnection.onClosed { exception ->
                Log.e(TAG, "SignalR connection closed.", exception)
                // Verifică dacă scope-ul principal al serviciului este încă activ și dacă jobul nu a fost anulat extern
                if (serviceScope.isActive && connectionJob?.isCancelled == false) {
                    attemptReconnect()
                }
            }

            try {
                Log.d(TAG, "Before localHubConnection.start().awaitFutureVoid()")
                // Apelul funcției suspendate
                val startFuture = localHubConnection.start()
                (startFuture as? CompletableFuture<Void>)?.awaitFutureVoid()
                Log.d(TAG, "After localHubConnection.start().awaitFutureVoid()")

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
        // Simplificăm verificarea, lăsăm startConnection să gestioneze dacă un job e deja activ
        Log.d(TAG, "Scheduling reconnect attempt...")
        serviceScope.launch { // Folosește serviceScope
            Log.d(TAG, "Attempting to reconnect in ${RECONNECT_DELAY_MS / 1000} seconds...")
            delay(RECONNECT_DELAY_MS)
            if (this.isActive) { // Verifică dacă această corutină de reconectare este încă activă
                Log.d(TAG, "Reconnect coroutine active, proceeding with stop and start.")
                val currentHub = hubConnection // Copie locală pentru thread-safety
                if (currentHub != null) {
                    try {
                        Log.d(TAG, "Attempting to stop connection in reconnect...")
                        (currentHub.stop() as? CompletableFuture<Void>)?.awaitFutureVoid()
                        Log.d(TAG, "Connection stopped in reconnect.")
                    } catch (e: Exception) {
                        Log.w(TAG, "Exception during stop in reconnect: ${e.message}")
                    }
                }
                hubConnection = null // Asigură-te că e null înainte de a încerca să reconectezi
                startConnection() // startConnection va crea un nou job
            } else {
                Log.d(TAG, "Reconnect attempt aborted: Coroutine scope for reconnect is no longer active.")
            }
        }
    }

    fun stopConnection(cancelOuterJob: Boolean = true) { // `cancelOuterJob` e mai mult un flag pentru logica internă
        Log.d(TAG, "stopConnection called. cancelOuterJob: $cancelOuterJob")
        val jobToCancel = if (cancelOuterJob) connectionJob else null
        val currentHub = hubConnection // Copie locală

        // Oprim conexiunea într-o nouă corutină pentru a nu bloca apelantul
        // și pentru a putea folosi funcții suspendate dacă e nevoie
        serviceScope.launch {
            if (currentHub != null) {
                if (currentHub.connectionState == HubConnectionState.CONNECTED || currentHub.connectionState == HubConnectionState.CONNECTING) {
                    try {
                        Log.d(TAG, "Attempting to stop hub connection...")
                        (currentHub.stop() as? CompletableFuture<Void>)?.awaitFutureVoid()
                        Log.i(TAG, "SignalR connection stopped successfully via stopConnection call.")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping SignalR connection via stopConnection call: ${e.message}", e)
                    }
                }
            }
            hubConnection = null // Eliberează referința

            if (jobToCancel != null && jobToCancel.isActive) {
                Log.d(TAG, "Cancelling connectionJob.")
                jobToCancel.cancel()
            }
            if (cancelOuterJob) { // Dacă e un stop final, connectionJob e setat la null
                connectionJob = null
            }
        }
    }
}