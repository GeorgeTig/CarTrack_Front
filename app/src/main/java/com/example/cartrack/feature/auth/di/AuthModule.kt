package com.example.cartrack.feature.auth.di

import com.example.cartrack.feature.auth.data.api.AuthApi
import com.example.cartrack.feature.auth.data.api.AuthApiImpl
import com.example.cartrack.feature.auth.domain.repository.AuthRepository
import com.example.cartrack.feature.auth.domain.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton // Scope matches dependencies (HttpClient is Singleton)
    abstract fun bindAuthApi(
        authApiServiceImpl: AuthApiImpl
    ): AuthApi

    @Binds
    @Singleton // Scope matches dependencies (AuthApi, TokenManager are Singleton)
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}