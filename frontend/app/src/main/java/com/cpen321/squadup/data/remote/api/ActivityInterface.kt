package com.cpen321.squadup.data.remote.api

import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.remote.dto.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ActivityInterface {
    @GET("group/activities")
    suspend fun getActivities(
        @Header("Authorization") authHeader: String,
        @Query("joinCode") joinCode: String  // Changed from @Body to @Query
    ): Response<ApiResponse<List<Activity>>>

    @POST("group/activities/select")
    suspend fun selectActivity(
        @Header("Authorization") authHeader: String,
        @Body request: SelectActivityRequest
    ): Response<ApiResponse<Unit>>
}

data class SelectActivityRequest(
    val joinCode: String,
    val placeId: String
)

