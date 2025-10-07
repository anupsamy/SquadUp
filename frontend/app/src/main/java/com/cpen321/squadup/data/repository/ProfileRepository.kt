package com.cpen321.squadup.data.repository

import com.cpen321.squadup.data.remote.dto.User

interface ProfileRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(name: String, bio: String): Result<User>
    suspend fun updateProfilePicture(profilePictureUrl: String): Result<User>
    suspend fun updateUserHobbies(hobbies: List<String>): Result<User>
    suspend fun getAvailableHobbies(): Result<List<String>>
}