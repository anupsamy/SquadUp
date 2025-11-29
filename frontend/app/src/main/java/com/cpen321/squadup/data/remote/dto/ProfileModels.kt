package com.cpen321.squadup.data.remote.dto

data class UpdateProfileRequest(
    val name: String? = null,
    val address: Address? = null,
    val transitType: String? = null,
    val profilePicture: String? = null,
)

data class ProfileData(
    val user: User,
)

data class User(
    val _id: String,
    val email: String,
    val name: String,
    val address: Address?,
    val transitType: TransitType?,
    val profilePicture: String,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class UploadImageData(
    val image: String,
)
