package com.example.cartrack.core.di

import com.example.cartrack.core.data.api.*
import com.example.cartrack.core.data.repository.*
import com.example.cartrack.core.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

    // API Binds
    @Binds @Singleton abstract fun bindVehicleApi(impl: VehicleApiImpl): VehicleApi
    @Binds @Singleton abstract fun bindUserApi(impl: UserApiImpl): UserApi
    @Binds @Singleton abstract fun bindNotificationApi(impl: NotificationApiImpl): NotificationApi
    @Binds @Singleton abstract fun bindVinDecoderApi(impl: VinDecoderApiImpl): VinDecoderApi
    @Binds @Singleton abstract fun bindWeatherApi(impl: WeatherApiImpl): WeatherApi // <-- LINIA NOUĂ

    // Repository Binds
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds @Singleton abstract fun bindVinDecoderRepository(impl: VinDecoderRepositoryImpl): VinDecoderRepository
}