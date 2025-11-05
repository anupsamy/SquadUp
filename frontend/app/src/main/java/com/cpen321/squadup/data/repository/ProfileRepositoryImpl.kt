package com.cpen321.squadup.data.repository

import android.content.Context
import android.util.Log
import com.cpen321.squadup.data.local.preferences.TokenManager
import com.cpen321.squadup.data.remote.api.RetrofitClient
import com.cpen321.squadup.data.remote.api.UserInterface
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.TransitType
import com.cpen321.squadup.data.remote.dto.UpdateProfileRequest
import com.cpen321.squadup.data.remote.dto.User
import com.cpen321.squadup.utils.JsonUtils.parseErrorMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userInterface: UserInterface,
    private val tokenManager: TokenManager
) : ProfileRepository {

    companion object {
        private const val TAG = "ProfileRepositoryImpl"
    }

    override suspend fun getProfile(): Result<User> {
        return try {
            val response = userInterface.getProfile("") // Auth header is handled by interceptor
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage =
                    parseErrorMessage(errorBodyString, "Failed to fetch user information.")
                Log.e(TAG, "Failed to get profile: $errorMessage")
                tokenManager.clearToken()
                RetrofitClient.setAuthToken(null)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while getting profile", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while getting profile", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while getting profile", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while getting profile: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(name: String, address: Address?, transitType: TransitType?): Result<User> {
        return try {

            fun String.sanitizeCRLF() = this
                .replace("\r\n", " ")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim()

            val sanitizedAddress = address?.let {
                it.copy(
                    formatted = it.formatted.sanitizeCRLF(),
                    placeId = it.placeId?.sanitizeCRLF(),
                    components = it.components?.let { comp ->
                        comp.copy(
                            streetNumber = comp.streetNumber?.sanitizeCRLF(),
                            route = comp.route?.sanitizeCRLF(),
                            city = comp.city?.sanitizeCRLF(),
                            province = comp.province?.sanitizeCRLF(),
                            country = comp.country?.sanitizeCRLF(),
                            postalCode = comp.postalCode?.sanitizeCRLF()
                        )
                    }
                )
            }

            val updateRequest = UpdateProfileRequest(
                name = name.sanitizeCRLF(),  // Sanitize name too!
                address = sanitizedAddress,
                transitType = transitType?.name?.lowercase()
            )
            
            val response = userInterface.updateProfile("", updateRequest) // Auth header is handled by interceptor
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update profile.")
                Log.e(TAG, "Failed to update profile: $errorMessage")
                Result.failure(Exception(errorMessage))
            }

        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while updating profile", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while updating profile", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while updating profile", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while updating profile: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePicture(profilePictureUrl: String): Result<User> {
        return try {

            val updateRequest = UpdateProfileRequest(profilePicture = profilePictureUrl)
            val response = userInterface.updateProfile("", updateRequest) // Auth header handled by interceptor

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.user)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update profile picture.")
                Log.e(TAG, "Failed to update profile picture: $errorMessage")
                Result.failure(Exception(errorMessage))
            }

        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while updating profile picture", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while updating profile picture", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while updating profile picture", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while updating profile picture: ${e.code()}", e)
            Result.failure(e)
        }
    }
}
