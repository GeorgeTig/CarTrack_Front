package com.example.cartrack.feature.addvehicle.di

import com.example.cartrack.feature.addvehicle.data.api.VinDecoderApi // Import from this feature
import com.example.cartrack.feature.addvehicle.data.api.VinDecoderApiImpl // Import from this feature
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepository // Import from this feature
import com.example.cartrack.feature.addvehicle.domain.repository.VinDecoderRepositoryImpl // Import from this feature
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent // Install in ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped // Scope bindings to ViewModel

@Module
@InstallIn(ViewModelComponent::class) // Scoped to ViewModels using these dependencies
abstract class AddVehicleModule {

    @Binds
    @ViewModelScoped // Scope to ViewModel lifecycle
    abstract fun bindVinDecoderApi(
        vinDecoderApiServiceImpl: VinDecoderApiImpl
    ): VinDecoderApi

    @Binds
    @ViewModelScoped // Scope to ViewModel lifecycle
    abstract fun bindVinDecoderRepository(
        vinDecoderRepositoryImpl: VinDecoderRepositoryImpl
    ): VinDecoderRepository

    // TODO: Add bindings for Saving vehicle API/Repo here later if needed
}