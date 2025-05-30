package com.example.cartrack.feature.addvehicle.di

import com.example.cartrack.feature.addvehicle.data.api.VehicleApi
import com.example.cartrack.feature.addvehicle.data.api.VehicleApiImpl
import com.example.cartrack.feature.addvehicle.data.api.VinDecoderApi
import com.example.cartrack.feature.addvehicle.data.api.VinDecoderApiImpl
import com.example.cartrack.feature.addvehicle.domain.repository.SaveVehicleRepository
import com.example.cartrack.feature.addvehicle.domain.repository.SaveVehicleRepositoryImpl
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepository
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class AddVehicleModule {

    @Binds
    @ViewModelScoped
    abstract fun bindVinDecoderApi(
        vinDecoderApiServiceImpl: VinDecoderApiImpl
    ): VinDecoderApi

    @Binds
    @ViewModelScoped
    abstract fun bindVinDecoderRepository(
        vinDecoderRepositoryImpl: VinDecoderRepositoryImpl
    ): VinDecoderRepository

    @Binds
    @ViewModelScoped
    abstract fun bindVehicleApi(
        vehicleApiImpl: VehicleApiImpl
    ): VehicleApi

    @Binds
    @ViewModelScoped
    abstract fun bindSaveVehicleRepository(
        saveVehicleRepositoryImpl: SaveVehicleRepositoryImpl
    ): SaveVehicleRepository
}