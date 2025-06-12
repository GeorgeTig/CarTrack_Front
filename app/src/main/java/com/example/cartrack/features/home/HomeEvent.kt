package com.example.cartrack.features.home

sealed class HomeEvent {
    data class ShowToast(val message: String) : HomeEvent()
}