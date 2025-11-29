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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesModule {
    @Provides
    @Singleton
    fun providePlacesClient(
        @ApplicationContext context: Context,
    ): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.GOOGLE_PLACES_API_KEY)
        }
        return Places.createClient(context)
    }
}

@Singleton
class PlacesRepository
    @Inject
    constructor(
        private val placesClient: PlacesClient,
    ) {
        /**
         * Returns a list of autocomplete predictions for a given query string.
         */
        suspend fun getPredictions(query: String): List<AutocompletePrediction> {
            if (query.isBlank()) return emptyList()

            val request =
                FindAutocompletePredictionsRequest
                    .builder()
                    .setQuery(query)
                    .build()

            return try {
                val response = placesClient.findAutocompletePredictions(request).await()
                response.autocompletePredictions
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
        }

        /**
         * Fetches full place details for a given placeId and converts it to our Address model.
         */
        suspend fun fetchPlace(placeId: String): Address? {
            val fields =
                listOf(
                    Place.Field.ID,
                    Place.Field.LOCATION,
                    Place.Field.FORMATTED_ADDRESS,
                    Place.Field.ADDRESS_COMPONENTS,
                )

            val request = FetchPlaceRequest.builder(placeId, fields).build()

            return try {
                val response = placesClient.fetchPlace(request).await()
                val place = response.place
                Address(
                    formatted = place.formattedAddress ?: "",
                    placeId = place.id,
                    lat = place.location?.latitude, // Used to be latLng
                    lng = place.location?.longitude, // Used to be latLng
                    components = place.addressComponents?.asAddressComponents(),
                )
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        private fun com.google.android.libraries.places.api.model.AddressComponents.asAddressComponents() =
            AddressComponents(
                streetNumber = getComponent("street_number"),
                route = getComponent("route"),
                city = getComponent("locality"),
                province = getComponent("administrative_area_level_1"),
                country = getComponent("country"),
                postalCode = getComponent("postal_code"),
            )

        private fun com.google.android.libraries.places.api.model.AddressComponents.getComponent(type: String) =
            this.asList().firstOrNull { it.types.contains(type) }?.name
    }
