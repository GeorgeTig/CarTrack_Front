package com.example.cartrack.core.di

import com.example.cartrack.core.data.api.*
import com.example.cartrack.core.data.repository.*
import com.example.cartrack.core.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

// Modul pentru legarea implementărilor API
@Module
@InstallIn(SingletonComponent::class)
abstract class ApiModule {

    @Binds
    @Singleton
    abstract fun bindAuthApi(impl: AuthApiImpl): AuthApi

    @Binds
    @Singleton
    abstract fun bindVehicleApi(impl: VehicleApiImpl): VehicleApi

    @Binds
    @Singleton
    abstract fun bindUserApi(impl: UserApiImpl): UserApi

    @Binds
    @Singleton
    abstract fun bindNotificationApi(impl: NotificationApiImpl): NotificationApi
}

// Modul pentru legarea implementărilor Repository
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}

// Modul pentru a furniza instanțe specifice de AuthApi, deoarece
// @Binds nu poate fi folosit cu @Qualifier în acest mod.
@Module
@InstallIn(SingletonComponent::class)
object AuthApiProviderModule {

    @Provides
    @Singleton
    @AuthenticatedAuthApi
    fun provideAuthenticatedAuthApi(@AuthenticatedHttpClient client: HttpClient): AuthApi {
        return AuthApiImpl(client)
    }

    @Provides
    @Singleton
    @UnauthenticatedAuthApi
    fun provideUnauthenticatedAuthApi(@UnauthenticatedHttpClient client: HttpClient): AuthApi {
        return AuthApiImpl(client)
    }
}