package com.cpen321.squadup.data.remote.api

import com.cpen321.squadup.data.remote.dto.ApiResponse
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.CreateGroupRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GroupInterface {
    @POST("group/create")
    suspend fun createGroup(
        @Header("Authorization") authHeader: String,
        @Body request: CreateGroupRequest
    ): Response<ApiResponse<GroupData>>
}