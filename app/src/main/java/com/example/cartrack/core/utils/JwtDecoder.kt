package com.example.cartrack.core.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.cartrack.core.storage.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to decode JWT tokens and extract claims.
 * Requires the `com.auth0:java-jwt` dependency.
 */
@Singleton
class JwtDecoder @Inject constructor(
    private val tokenManager: TokenManager
) {

    /**
     * Attempts to decode the currently stored JWT and extract the client ID (or other user identifier).
     * Returns the client ID as an Int, or null if the token is missing, invalid,
     * or doesn't contain the expected claim as an integer.
     *
     * IMPORTANT: Replace "sub" with the actual claim name used by your backend API!
     *
     * @return The client ID as an Int?, or null on failure.
     */
    suspend fun getClientIdFromToken(): Int? {
        val tokenString = tokenManager.tokenFlow.firstOrNull()
        if (tokenString.isNullOrBlank()) {
            System.err.println("JwtDecoder: No token found in TokenManager.")
            return null
        }

        return try {
            val decodedJWT: DecodedJWT = JWT.decode(tokenString)
            val claimName = "sub"

            val clientIdClaim = decodedJWT.getClaim(claimName)

            if (clientIdClaim.isNull || clientIdClaim.isMissing) {
                System.err.println("JwtDecoder: JWT claim '$claimName' is missing or null.")
                return null
            }


            val clientIdString = clientIdClaim.asString()
            val clientId = clientIdString?.toIntOrNull()

            if (clientId == null) {
                System.err.println("JwtDecoder: JWT claim '$claimName' exists but could not be parsed as an integer (value: '$clientIdString').")
            }
            clientId

        } catch (e: JWTDecodeException) {
            System.err.println("JwtDecoder: Failed to decode JWT: ${e.message}")
            null
        } catch (e: NumberFormatException) {
            System.err.println("JwtDecoder: Error parsing claim  to Int: ${e.message}")
            null
        } catch (e: Exception) {
            System.err.println("JwtDecoder: Unexpected error getting claim : ${e.message}")
            null
        }
    }
}