package com.cpen321.squadup.data.remote.api

import com.cpen321.squadup.data.remote.dto.ApiResponse
import com.cpen321.squadup.data.remote.dto.HobbiesData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface HobbyInterface {
    @GET("hobbies")
    suspend fun getAvailableHobbies(@Header("Authorization") authHeader: String): Response<ApiResponse<HobbiesData>>
}
