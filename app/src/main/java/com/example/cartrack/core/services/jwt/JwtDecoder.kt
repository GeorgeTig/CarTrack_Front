package com.example.cartrack.core.services.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtDecoder @Inject constructor() {

    fun getClientIdFromToken(tokenString: String?): Int? {
        if (tokenString.isNullOrBlank()) {
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
            clientIdString?.toIntOrNull()
        } catch (e: JWTDecodeException) {
            System.err.println("JwtDecoder: Failed to decode JWT: ${e.message}")
            null
        } catch (e: Exception) {
            System.err.println("JwtDecoder: Unexpected error getting claim: ${e.message}")
            null
        }
    }
}