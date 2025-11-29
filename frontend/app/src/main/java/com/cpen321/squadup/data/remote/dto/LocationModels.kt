package com.cpen321.squadup.data.remote.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class Address(
    val formatted: String,
    val placeId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val components: AddressComponents? = null,
)

data class GeoLocation(
    val lat: Double? = null,
    val lng: Double? = null,
)

data class SquadGoal(
    val location: GeoLocation,
)

data class MidpointActivitiesResponse(
    val midpoint: SquadGoal,
    val activities: List<Activity>,
)

fun parseMidpointString(midpointStr: String?): SquadGoal? {
    if (midpointStr.isNullOrBlank()) return null

    val parts = midpointStr.trim().split(" ")
    if (parts.size != 2) return null

    val lat = parts[0].toDoubleOrNull()
    val lng = parts[1].toDoubleOrNull()

    return if (lat != null && lng != null) {
        SquadGoal(GeoLocation(lat, lng))
    } else {
        null
    }
}

data class AddressComponents(
    val streetNumber: String? = null,
    val route: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val postalCode: String? = null,
)

@Serializable
enum class TransitType {
    @SerializedName("driving")
    DRIVING,

    @SerializedName("walking")
    WALKING,

    @SerializedName("bicycling")
    BICYCLING,

    @SerializedName("transit")
    TRANSIT,
}

data class Activity(
    val name: String,
    val placeId: String,
    val address: String,
    val rating: Double,
    val userRatingsTotal: Int,
    val priceLevel: Int,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val businessStatus: String,
    val isOpenNow: Boolean,
)

enum class ActivityType(
    val storedValue: String,
    val displayName: String,
) {
    RESTAURANT("restaurant", "RESTAURANT"),
    CAFE("cafe", "CAFE"),
    BAR("bar", "BAR"),
    PARK("park", "PARK"),
    GYM("gym", "GYM"),
    BOWLING_ALLEY("bowling_alley", "BOWLING ALLEY"),
    MOVIE_THEATER("movie_theater", "MOVIE THEATER"),
    NIGHT_CLUB("night_club", "NIGHT CLUB"),
    AMUSEMENT_PARK("amusement_park", "AMUSEMENT PARK"),
    MUSEUM("museum", "MUSEUM"),
    ;

    companion object {
        fun getActivity(value: String): ActivityType? = entries.find { it.storedValue == value }
    }
}
