package com.cpen321.squadup.data.remote.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


data class Address(
    val formatted: String,
    val placeId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val components: AddressComponents? = null
)

data class GeoLocation(
    val lat: Double? = null,
    val lng: Double? = null,
)

data class SquadGoal(
    val location: GeoLocation
)

data class AddressComponents(
    val streetNumber: String? = null,
    val route: String? = null,
    val city: String? = null,
    val province: String? = null,
    val country: String? = null,
    val postalCode: String? = null
)

@Serializable
enum class TransitType {
    @SerializedName("driving") DRIVING,
    @SerializedName("walking") WALKING,
    @SerializedName("bicycling") BICYCLING,
    @SerializedName("transit") TRANSIT
}