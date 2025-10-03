package com.cpen321.usermanagement.data.remote.api

import com.cpen321.usermanagement.data.remote.dto.ApiResponse
import com.cpen321.usermanagement.data.remote.dto.ProfileData
import com.cpen321.usermanagement.data.remote.dto.UpdateProfileRequest
import com.cpen321.usermanagement.data.remote.dto.UploadImageData
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UserInterface {
    @GET("user/profile")
    suspend fun getProfile(@Header("Authorization") authHeader: String): Response<ApiResponse<ProfileData>>

    @POST("user/profile")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<ProfileData>>

    @DELETE("user/profile")
    suspend fun deleteProfile(
        @Header("Authorization") authHeader: String,
    ): Response<Unit>
}

interface ImageInterface {
    @Multipart
    @POST("media/upload")
    suspend fun uploadPicture(
        @Header("Authorization") authHeader: String,
        @Part media: MultipartBody.Part
    ): Response<ApiResponse<UploadImageData>>
}