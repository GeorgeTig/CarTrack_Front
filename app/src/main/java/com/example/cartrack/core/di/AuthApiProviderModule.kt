package com.example.cartrack.core.di

import com.example.cartrack.core.data.api.AuthApi
import com.example.cartrack.core.data.api.AuthApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

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