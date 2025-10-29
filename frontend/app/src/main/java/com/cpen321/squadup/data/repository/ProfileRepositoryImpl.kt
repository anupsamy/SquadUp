package com.cpen321.squadup.data.repository

import android.content.Context
import android.util.Log
import com.cpen321.squadup.data.local.preferences.TokenManager
import com.cpen321.squadup.data.remote.api.HobbyInterface
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
//            Log.d(TAG, "=== CRLF Detection Debug ===")
//            Log.d(TAG, "name contains \\r: ${name.contains("\r")}")
//            Log.d(TAG, "name contains \\n: ${name.contains("\n")}")
//            Log.d(TAG, "name value: '$name'")
//
//            address?.let {
//                Log.d(TAG, "formatted contains \\r: ${it.formatted.contains("\r")}")
//                Log.d(TAG, "formatted contains \\n: ${it.formatted.contains("\n")}")
//                Log.d(TAG, "formatted value: '${it.formatted}'")
//
//                Log.d(TAG, "placeId contains \\r: ${it.placeId?.contains("\r")}")
//                Log.d(TAG, "placeId contains \\n: ${it.placeId?.contains("\n")}")
//                Log.d(TAG, "placeId value: '${it.placeId}'")
//
//                it.components?.let { comp ->
//                    Log.d(TAG, "streetNumber contains \\r: ${comp.streetNumber?.contains("\r")}")
//                    Log.d(TAG, "streetNumber contains \\n: ${comp.streetNumber?.contains("\n")}")
//                    Log.d(TAG, "streetNumber value: '${comp.streetNumber}'")
//
//                    Log.d(TAG, "route contains \\r: ${comp.route?.contains("\r")}")
//                    Log.d(TAG, "route contains \\n: ${comp.route?.contains("\n")}")
//                    Log.d(TAG, "route value: '${comp.route}'")
//
//                    Log.d(TAG, "city contains \\r: ${comp.city?.contains("\r")}")
//                    Log.d(TAG, "city contains \\n: ${comp.city?.contains("\n")}")
//                    Log.d(TAG, "city value: '${comp.city}'")
//
//                    Log.d(TAG, "province contains \\r: ${comp.province?.contains("\r")}")
//                    Log.d(TAG, "province contains \\n: ${comp.province?.contains("\n")}")
//                    Log.d(TAG, "province value: '${comp.province}'")
//
//                    Log.d(TAG, "country contains \\r: ${comp.country?.contains("\r")}")
//                    Log.d(TAG, "country contains \\n: ${comp.country?.contains("\n")}")
//                    Log.d(TAG, "country value: '${comp.country}'")
//
//                    Log.d(TAG, "postalCode contains \\r: ${comp.postalCode?.contains("\r")}")
//                    Log.d(TAG, "postalCode contains \\n: ${comp.postalCode?.contains("\n")}")
//                    Log.d(TAG, "postalCode value: '${comp.postalCode}'")
//                } ?: Log.d(TAG, "components is null")
//            } ?: Log.d(TAG, "address is null")
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
                transitType = transitType?.name?.lowercase())
            Log.d(TAG, "UpdateProfileRequest: $updateRequest")
            val response = userInterface.updateProfile("", updateRequest) // Auth header is handled by interceptor
            Log.d("update profile test", "update profile test" + response.body()?.message.toString())
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

//    override suspend fun updateUserHobbies(hobbies: List<String>): Result<User> {
//        return try {
//            val updateRequest = UpdateProfileRequest
//            val response = userInterface.updateProfile("", updateRequest) // Auth header is handled by interceptor
//            if (response.isSuccessful && response.body()?.data != null) {
//                Result.success(response.body()!!.data!!.user)
//            } else {
//                val errorBodyString = response.errorBody()?.string()
//                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update hobbies.")
//
//                Log.e(TAG, "Failed to update hobbies: $errorMessage")
//                Result.failure(Exception(errorMessage))
//            }
//        } catch (e: java.net.SocketTimeoutException) {
//            Log.e(TAG, "Network timeout while updating hobbies", e)
//            Result.failure(e)
//        } catch (e: java.net.UnknownHostException) {
//            Log.e(TAG, "Network connection failed while updating hobbies", e)
//            Result.failure(e)
//        } catch (e: java.io.IOException) {
//            Log.e(TAG, "IO error while updating hobbies", e)
//            Result.failure(e)
//        } catch (e: retrofit2.HttpException) {
//            Log.e(TAG, "HTTP error while updating hobbies: ${e.code()}", e)
//            Result.failure(e)
//        }
//    }

//    override suspend fun getAvailableHobbies(): Result<List<String>> {
//        return try {
//            val response = hobbyInterface.getAvailableHobbies("") // Auth header is handled by interceptor
//            if (response.isSuccessful && response.body()?.data != null) {
//                Result.success(response.body()!!.data!!.hobbies)
//            } else {
//                val errorBodyString = response.errorBody()?.string()
//                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch hobbies.")
//                Log.e(TAG, "Failed to get available hobbies: $errorMessage")
//                Result.failure(Exception(errorMessage))
//            }
//        } catch (e: java.net.SocketTimeoutException) {
//            Log.e(TAG, "Network timeout while getting available hobbies", e)
//            Result.failure(e)
//        } catch (e: java.net.UnknownHostException) {
//            Log.e(TAG, "Network connection failed while getting available hobbies", e)
//            Result.failure(e)
//        } catch (e: java.io.IOException) {
//            Log.e(TAG, "IO error while getting available hobbies", e)
//            Result.failure(e)
//        } catch (e: retrofit2.HttpException) {
//            Log.e(TAG, "HTTP error while getting available hobbies: ${e.code()}", e)
//            Result.failure(e)
//        }
//    }
}
