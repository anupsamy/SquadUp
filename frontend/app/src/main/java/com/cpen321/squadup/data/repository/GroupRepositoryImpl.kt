package com.cpen321.squadup.data.repository

import android.util.Log
import com.cpen321.squadup.data.local.preferences.TokenManager
import com.cpen321.squadup.data.remote.api.GroupInterface
import com.cpen321.squadup.data.remote.dto.CreateGroupRequest
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupsDataAll
import com.cpen321.squadup.data.remote.dto.GroupLeaderUser
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.utils.JsonUtils.parseErrorMessage
import javax.inject.Inject
import javax.inject.Singleton

import com.cpen321.squadup.data.remote.api.RetrofitClient
import com.cpen321.squadup.data.remote.dto.ApiResponse
import retrofit2.Response

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupInterface: GroupInterface,
    private val tokenManager: TokenManager
) : GroupRepository {

    companion object {
        private const val TAG = "GroupRepositoryImpl"
    }//NOTE: Everything is returning null, add nesting layer to frontend receiver type
    override suspend fun getGroupByJoinCode(joinCode: String): Result<GroupDataDetailed> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val response = groupInterface.getGroupByJoinCode("Bearer $authToken", joinCode)
            Log.d(TAG, "GroupRepImpl getGroupByJoinCode response: ${response.body()!!.data!!.group}")
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.group) // Return GroupDataDetailed directly
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch group by joinCode.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getGroups(): Result<List<GroupDataDetailed>> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val response = groupInterface.getGroups("Bearer $authToken") // Ensure this is the correct endpoint
            val groupsDataAll = response.body()?.data
            Log.d(TAG, "GroupRepImpl getGroups response: ${groupsDataAll}")
            if (response.isSuccessful && groupsDataAll != null) {
                val groups = groupsDataAll.groups // Directly use the list of GroupDataDetailed
                Result.success(groups)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch groups.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createGroup(
        groupName: String, 
        meetingTime: String, 
        groupLeaderId: GroupLeaderUser, 
        expectedPeople: Number
        ): Result<GroupData> {
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