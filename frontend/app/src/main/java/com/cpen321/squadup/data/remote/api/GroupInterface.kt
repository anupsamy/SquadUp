package com.cpen321.squadup.data.remote.api

import com.cpen321.squadup.data.remote.dto.ActivityCoordinates
import com.cpen321.squadup.data.remote.dto.ApiResponse
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.GroupsDataAll
import com.cpen321.squadup.data.remote.dto.CreateGroupRequest
import com.cpen321.squadup.data.remote.dto.UpdateGroupRequest
import com.google.android.gms.maps.model.LatLng
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface GroupInterface {
    @GET("group/info")
    suspend fun getGroups(
        @Header("Authorization") authHeader: String
    ): Response<ApiResponse<GroupsDataAll>>

    @GET("group/{joinCode}")
    suspend fun getGroupByJoinCode(
        @Header("Authorization") authHeader: String,
        @retrofit2.http.Path("joinCode") joinCode: String
    ): Response<ApiResponse<GroupData>>

    @GET("group/midpoints")
    suspend fun getMidpoints(
        @Header("Authorization") authHeader: String,
        @Query("joinCode") joinCode: String
    ): Response<ApiResponse<List<ActivityCoordinates>>>

    @POST("group/create")
    suspend fun createGroup(
        @Header("Authorization") authHeader: String,
        @Body request: CreateGroupRequest
    ): Response<ApiResponse<GroupData>>

    @POST("group/update")
    suspend fun joinGroup(
        @Header("Authorization") authHeader: String,
        @Body request: UpdateGroupRequest
    ): Response<Unit>

    @DELETE("group/delete/{joinCode}")
    suspend fun deleteGroup(
        @Header("Authorization") authHeader: String,
        @retrofit2.http.Path("joinCode") joinCode: String
    ): Response<Unit>
}