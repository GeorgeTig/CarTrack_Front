package com.example.cartrack.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedAuthApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedAuthApi