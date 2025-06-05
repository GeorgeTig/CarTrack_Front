package com.example.cartrack.feature.profile.di

import com.example.cartrack.feature.profile.data.api.UserApi
import com.example.cartrack.feature.profile.data.api.UserApiImpl
import com.example.cartrack.feature.profile.domain.repository.UserRepository
import com.example.cartrack.feature.profile.domain.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class) // Sau SingletonComponent dacă e mai potrivit
abstract class ProfileModule {

    @Binds
    @ViewModelScoped
    abstract fun bindUserApi(
        userApiImpl: UserApiImpl
    ): UserApi

    @Binds
    @ViewModelScoped
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}