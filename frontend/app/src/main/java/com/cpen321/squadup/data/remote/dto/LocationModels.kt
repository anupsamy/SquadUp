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
    val isOpenNow: Boolean
)
