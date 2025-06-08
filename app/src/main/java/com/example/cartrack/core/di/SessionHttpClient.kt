package com.example.cartrack.core.di

import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

// Această clasă va acționa ca un "container" pentru clientul nostru de sesiune.
// Este un Singleton, dar conținutul său poate fi resetat.
@Singleton
class SessionHttpClient @Inject constructor(
    // Injectăm un Provider, nu direct HttpClient.
    // Asta ne permite să cerem o INSTANȚĂ NOUĂ de fiecare dată când apelăm .get()
    private val httpClientProvider: Provider<HttpClient>
) {
    // Lazy initialization: clientul este creat doar când este accesat prima dată.
    private var client: HttpClient? = null

    fun get(): HttpClient {
        if (client == null) {
            // Dacă clientul nu există (sau a fost resetat), creăm unul nou.
            client = httpClientProvider.get()
        }
        return client!!
    }

    // Metoda magică: la logout, vom apela asta pentru a distruge clientul vechi.
    fun reset() {
        client?.close() // Închidem conexiunile clientului Ktor existent.
        client = null   // Setăm referința la null.
    }
}