package com.example.cartrack.core.di

import javax.inject.Qualifier

// Pentru a diferenția între clientul HTTP cu și fără token de autentificare
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedHttpClient

// Pentru a diferenția între interfața AuthApi care folosește clientul autentificat
// și cea care folosește clientul neautentificat (pentru login, register, refresh)
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedAuthApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedAuthApi