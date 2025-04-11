package com.example.cartrack

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // <<< MUST be here
class CarTrackApp : Application() {}