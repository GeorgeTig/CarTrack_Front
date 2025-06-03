package com.example.cartrack.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedHttpClient

@Qualifier // <--- ADAUGĂ ACEASTA
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedAuthApi

@Qualifier // <--- ADAUGĂ ACEASTA
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedAuthApi