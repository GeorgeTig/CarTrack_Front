package com.example.cartrack.feature.home.presentation.notifications.di

import com.example.cartrack.feature.home.presentation.notifications.data.api.NotificationApi
import com.example.cartrack.feature.home.presentation.notifications.data.api.NotificationApiImpl
import com.example.cartrack.feature.home.presentation.notifications.domain.repository.NotificationRepository
import com.example.cartrack.feature.home.presentation.notifications.domain.repository.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class) // Scoped la ViewModel
abstract class NotificationModule {

    @Binds
    @ViewModelScoped
    abstract fun bindNotificationApi(
        notificationApiImpl: NotificationApiImpl
    ): NotificationApi

    @Binds
    @ViewModelScoped
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}