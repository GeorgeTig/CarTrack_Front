package com.example.cartrack.core.di

import android.content.Context
import com.example.cartrack.core.storage.TokenManager
import com.example.cartrack.core.storage.TokenManagerImpl
import com.example.cartrack.core.storage.UserManager
import com.example.cartrack.core.storage.UserManagerImpl
import com.example.cartrack.core.storage.VehicleManager
import com.example.cartrack.core.storage.VehicleManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModules {

    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager = TokenManagerImpl(context)

    @Singleton
    @Provides
    fun provideVehicleManager(@ApplicationContext context: Context): VehicleManager = VehicleManagerImpl(context)

    @Singleton
    @Provides
    fun provideUserManager(@ApplicationContext context: Context): UserManager = UserManagerImpl(context)
}