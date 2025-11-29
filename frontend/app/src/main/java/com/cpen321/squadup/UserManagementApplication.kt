package com.cpen321.squadup

import android.app.Application
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UserManagementApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Google Places SDK
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(
                applicationContext,
                BuildConfig.GOOGLE_PLACES_API_KEY,
            )
        }
    }
}
