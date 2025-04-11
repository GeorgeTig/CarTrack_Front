package com.example.cartrack.core.utils

import com.auth0.jwt.JWT         // Use the static methods from this import
import com.auth0.jwt.exceptions.JWTDecodeException // Specific exception for decoding errors
import com.auth0.jwt.interfaces.DecodedJWT // The interface for the decoded token object
import com.example.cartrack.core.storage.TokenManager // Needs TokenManager interface to get token
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to decode JWT tokens and extract claims.
 * Requires the `com.auth0:java-jwt` dependency.
 */
@Singleton // Make it a Singleton as it depends on Singleton TokenManager
class JwtDecoder @Inject constructor( // Inject TokenManager
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
        // println("JwtDecoder: Attempting to decode token starting with ${tokenString.take(10)}...") // Log before decode

        return try {
            val decodedJWT: DecodedJWT = JWT.decode(tokenString)
            val claimName = "sub" // This is correct based on backend code

            val clientIdClaim = decodedJWT.getClaim(claimName)

            if (clientIdClaim.isNull || clientIdClaim.isMissing) {
                System.err.println("JwtDecoder: JWT claim '$claimName' is missing or null.")
                return null
            }

            // *** CHANGE: Get as String first, then parse to Int ***
            val clientIdString = clientIdClaim.asString() // Get the value as a String
            val clientId = clientIdString?.toIntOrNull() // Try to parse the String to an Int?

            if (clientId == null) {
                // Log if parsing failed (e.g., value was not a valid number string)
                System.err.println("JwtDecoder: JWT claim '$claimName' exists but could not be parsed as an integer (value: '$clientIdString').")
            } else {
                // Optional: Log success
                // println("JwtDecoder: Successfully parsed clientId $clientId from claim '$claimName'.")
            }
            clientId // Return the Int? result (will be null if parsing failed)

        } catch (e: JWTDecodeException) {
            System.err.println("JwtDecoder: Failed to decode JWT: ${e.message}")
            null
        } catch (e: NumberFormatException) {
            // Catch specific error if toIntOrNull wasn't used and asString was directly parsed
            System.err.println("JwtDecoder: Error parsing claim  to Int: ${e.message}")
            null
        } catch (e: Exception) {
            System.err.println("JwtDecoder: Unexpected error getting claim : ${e.message}")
            null
        }
    }
}