package com.example.cartrack.core.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.cartrack.core.storage.TokenManager // Asigură-te că TokenManager este importat corect
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to decode JWT tokens and extract claims.
 * Requires the `com.auth0:java-jwt` dependency.
 */
@Singleton
class JwtDecoder @Inject constructor(
    private val tokenManager: TokenManager // TokenManager injectat
) {

    /**
     * Attempts to decode the currently stored JWT and extract the client ID (or other user identifier).
     * Returns the client ID as an Int, or null if the token is missing, invalid,
     * or doesn't contain the expected claim as an integer.
     *
     * The claim name "sub" (subject) is commonly used for user/client ID.
     *
     * @return The client ID as an Int?, or null on failure.
     */
    suspend fun getClientIdFromToken(): Int? {
        // Folosește accessTokenFlow în loc de tokenFlow
        val tokenString = tokenManager.accessTokenFlow.firstOrNull()

        if (tokenString.isNullOrBlank()) {
            System.err.println("JwtDecoder: No access token found in TokenManager.")
            return null
        }

        return try {
            val decodedJWT: DecodedJWT = JWT.decode(tokenString)
            // "sub" este un claim standard pentru subject (user ID) în JWT.
            // Verifică dacă backend-ul tău folosește acest claim pentru ID-ul clientului.
            val claimName = "sub"

            val clientIdClaim = decodedJWT.getClaim(claimName)

            if (clientIdClaim.isNull || clientIdClaim.isMissing) {
                System.err.println("JwtDecoder: JWT claim '$claimName' is missing or null.")
                return null
            }

            // Încearcă să convertești claim-ul la String și apoi la Int.
            // Claim-ul "sub" este de obicei un String.
            val clientIdString = clientIdClaim.asString()
            val clientId = clientIdString?.toIntOrNull()

            if (clientId == null) {
                System.err.println("JwtDecoder: JWT claim '$claimName' exists (value: '$clientIdString') but could not be parsed as an integer.")
            }
            clientId

        } catch (e: JWTDecodeException) {
            System.err.println("JwtDecoder: Failed to decode JWT: ${e.message}")
            null
        } catch (e: NumberFormatException) {
            // Această excepție poate apărea dacă toIntOrNull() eșuează pe un string non-numeric valid.
            System.err.println("JwtDecoder: Error parsing claim  to Int: ${e.message}")
            null
        } catch (e: Exception) {
            // Orice altă excepție neașteptată în timpul decodării sau extragerii claim-ului.
            System.err.println("JwtDecoder: Unexpected error getting claim : ${e.message}")
            null
        }
    }
}