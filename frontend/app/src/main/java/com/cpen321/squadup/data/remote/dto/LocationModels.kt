package com.cpen321.squadup.data.remote.dto


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

enum class TransitType {
    DRIVING,
    WALKING,
    BICYCLING,
    TRANSIT
}