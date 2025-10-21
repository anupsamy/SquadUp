package com.cpen321.squadup.data.repository

import android.content.Context
import com.cpen321.squadup.BuildConfig
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.AddressComponents
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

import dagger.Module
import dagger.Provides


@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.GOOGLE_PLACES_API_KEY)
        }
        return Places.createClient(context)
    }
}


@Singleton
class PlacesRepository @Inject constructor(
    private val placesClient: PlacesClient
) {

    /**
     * Returns a list of autocomplete predictions for a given query string.
     */
    suspend fun getPredictions(query: String): List<AutocompletePrediction> {
        if (query.isBlank()) return emptyList()

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        return try {
            val response = placesClient.findAutocompletePredictions(request).await()
            response.autocompletePredictions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Fetches full place details for a given placeId and converts it to our Address model.
     */
    suspend fun fetchPlace(placeId: String): Address? {
        // ⚠️ FIX: Updated Place.Field constants
        val fields = listOf(
            Place.Field.ID,
            Place.Field.LOCATION, // Used to be LAT_LNG
            Place.Field.FORMATTED_ADDRESS, // Used to be ADDRESS
            Place.Field.ADDRESS_COMPONENTS
        )

        val request = FetchPlaceRequest.builder(placeId, fields).build()

        return try {
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            Address(
                // ⚠️ FIX: Accessing the new formattedAddress property
                formatted = place.formattedAddress ?: "",
                placeId = place.id,
                lat = place.location?.latitude, // Used to be latLng
                lng = place.location?.longitude, // Used to be latLng
                components = place.addressComponents?.asAddressComponents()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Helper extension to convert Google AddressComponents to our AddressComponents
     */
    private fun com.google.android.libraries.places.api.model.AddressComponents.asAddressComponents() =
        AddressComponents(
            // ⚠️ FIX: Replaced Place.Type enums with their String representations
            streetNumber = getComponent("street_number"),
            route = getComponent("route"),
            city = getComponent("locality"),
            province = getComponent("administrative_area_level_1"),
            country = getComponent("country"),
            postalCode = getComponent("postal_code")
        )

    // ⚠️ FIX: getComponent now accepts a String, since Place.Type address component enums were removed
    private fun com.google.android.libraries.places.api.model.AddressComponents.getComponent(type: String) =
        this.asList().firstOrNull { it.types.contains(type) }?.name
}
