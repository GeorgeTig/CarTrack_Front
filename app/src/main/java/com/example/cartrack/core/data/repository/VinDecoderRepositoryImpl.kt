package com.example.cartrack.core.data.repository

import com.example.cartrack.core.data.api.VinDecoderApi
import com.example.cartrack.core.data.api.safeApiCall
import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto
import com.example.cartrack.core.domain.repository.AuthRepository
import com.example.cartrack.core.domain.repository.VinDecoderRepository
import javax.inject.Inject
import javax.inject.Provider

class VinDecoderRepositoryImpl @Inject constructor(
    private val api: VinDecoderApi,
    private val authRepositoryProvider: Provider<AuthRepository>
) : VinDecoderRepository {

    override suspend fun decodeVin(vin: String): Result<List<VinDecodedResponseDto>> =
        safeApiCall(authRepositoryProvider, "VIN Decode") { api.decodeVin(vin) }
}