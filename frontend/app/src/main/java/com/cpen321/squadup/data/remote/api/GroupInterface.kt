package com.cpen321.squadup.data.remote.api

import com.cpen321.squadup.data.remote.dto.ApiResponse
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.CreateGroupRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroupInterface {
    @POST("group/create")
    suspend fun createGroup(
        @Header("Authorization") authHeader: String,
        @Body request: GroupData
    ): Response<ApiResponse<GroupData>>
}