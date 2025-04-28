package com.example.cartrack.feature.vehicle.di

import com.example.cartrack.feature.vehicle.data.api.VehicleApi
import com.example.cartrack.feature.vehicle.data.api.VehicleApiImpl
import com.example.cartrack.feature.vehicle.domain.repository.VehicleRepository
import com.example.cartrack.feature.vehicle.domain.repository.VehicleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VehicleModule {

    @Binds
    @Singleton
    abstract fun bindVehicleApi(
        vehicleApiServiceImpl: VehicleApiImpl
    ): VehicleApi

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository
}