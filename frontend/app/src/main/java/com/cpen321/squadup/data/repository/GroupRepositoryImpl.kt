package com.cpen321.squadup.data.repository

import android.util.Log
import com.cpen321.squadup.data.remote.api.GroupInterface
import com.cpen321.squadup.data.remote.dto.CreateGroupRequest
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.utils.JsonUtils.parseErrorMessage
import javax.inject.Inject
import javax.inject.Singleton

import com.cpen321.squadup.data.remote.api.RetrofitClient
import com.cpen321.squadup.data.remote.dto.ApiResponse
import retrofit2.Response

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupInterface: GroupInterface
) : GroupRepository {

    companion object {
        private const val TAG = "GroupRepositoryImpl"
    }

    override suspend fun createGroup(groupName: String, meetingTime: String, groupLeaderId: String, expectedPeople: Number): Result<GroupData> {
        return try {
            val request = CreateGroupRequest(
                groupName = groupName, 
                meetingTime = meetingTime, 
                groupLeaderId = groupLeaderId, 
                expectedPeople = expectedPeople
            )
            val response = groupInterface.createGroup("", request)
            //:Response<ApiResponse<GroupData>>
            Log.d(TAG, "GroupRepImpl response: ${response.body()}")
            Log.d(TAG, "GroupRepImpl groupdata: ${response.body()!!.data}")

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to create group.")
                Log.e(TAG, "Failed to create group: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while creating group", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while reating group", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while reating group", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while reating group: ${e.code()}", e)
            Result.failure(e)
        }
    }
}