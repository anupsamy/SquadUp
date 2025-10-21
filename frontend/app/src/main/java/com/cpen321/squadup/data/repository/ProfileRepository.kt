package com.cpen321.squadup.data.repository

import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.TransitType
import com.cpen321.squadup.data.remote.dto.User

interface ProfileRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(name: String, address: Address?, transitType: TransitType?): Result<User>
    suspend fun updateProfilePicture(profilePictureUrl: String): Result<User>
}