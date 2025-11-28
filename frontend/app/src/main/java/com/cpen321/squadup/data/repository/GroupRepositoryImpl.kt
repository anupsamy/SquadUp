package com.cpen321.squadup.data.repository

import android.health.connect.ReadRecordsRequestUsingIds
import android.util.Log
import com.cpen321.squadup.data.local.preferences.TokenManager
import com.cpen321.squadup.data.remote.api.ActivityInterface
import com.cpen321.squadup.data.remote.api.GroupInterface
import com.cpen321.squadup.data.remote.dto.CreateGroupRequest
import com.cpen321.squadup.data.remote.dto.UpdateGroupRequest
import com.cpen321.squadup.data.remote.dto.LeaveGroupRequest
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupUser
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.utils.JsonUtils.parseErrorMessage
import javax.inject.Inject
import javax.inject.Singleton

import com.cpen321.squadup.data.remote.api.SelectActivityRequest
import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.MidpointActivitiesResponse
import com.google.android.gms.maps.model.LatLng
import com.cpen321.squadup.data.remote.dto.SquadGoal
import com.cpen321.squadup.data.remote.dto.TransitType
import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupInterface: GroupInterface,
    private val activityInterface: ActivityInterface,
    private val tokenManager: TokenManager
) : GroupRepository {

    companion object {
        private const val TAG = "GroupRepositoryImpl"
    }//NOTE: Everything is returning null, add nesting layer to frontend receiver type
    override suspend fun getGroupByJoinCode(joinCode: String): Result<GroupDataDetailed> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val response = groupInterface.getGroupByJoinCode("Bearer $authToken", joinCode)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!.group) // Return GroupDataDetailed directly
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch group by joinCode.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun getGroups(): Result<List<GroupDataDetailed>> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val response = groupInterface.getGroups("Bearer $authToken") // Ensure this is the correct endpoint
            val groupsDataAll = response.body()?.data

            if (response.isSuccessful && groupsDataAll != null) {
                val groups = groupsDataAll.groups // Directly use the list of GroupDataDetailed
                Log.d(TAG, "GroupRepImpl getGroups response 2: ${groups}")
                Result.success(groups)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch groups.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun createGroup(
        groupName: String,
        meetingTime: String,
        groupLeaderId: GroupUser,
        expectedPeople: Number,
        activityType: String,
        autoMidpoint: Boolean
        ): Result<GroupData> {
        return try {
            val request = CreateGroupRequest(
                groupName = groupName,
                meetingTime = meetingTime,
                groupLeaderId = groupLeaderId,
                expectedPeople = expectedPeople,
                activityType = activityType,
                autoMidpoint = autoMidpoint
            )
            val response = groupInterface.createGroup("", request)

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

    override suspend fun deleteGroupByJoinCode(joinCode: String): Result<Unit> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val response = groupInterface.deleteGroup("Bearer $authToken", joinCode)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to delete group.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun joinGroup(joinCode: String): Result<Unit> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val request = UpdateGroupRequest(
                joinCode,
                null,
                null,
                null,
                null
            )

            val response = groupInterface.joinGroup(
                authHeader = "Bearer $authToken",
                request = request
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to join group.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun updateGroupSettings(
        joinCode: String,
        address: Address?,
        transitType: TransitType?,
        meetingTime: String?,
        expectedPeople: Int?,
        autoMidpoint: Boolean?,
        activityType: String?
    ): Result<Unit> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val request = UpdateGroupRequest(
                joinCode = joinCode,
                address = address,
                transitType = transitType,
                meetingTime = meetingTime,
                expectedPeople = expectedPeople,
                autoMidpoint = autoMidpoint,
                activityType = activityType
            )

            val response = groupInterface.updateGroup(
                authHeader = "Bearer $authToken",
                request = request
            )
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to join group.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Network timeout while updating group", e)
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "Network connection failed while updating group", e)
            Result.failure(e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while updating group", e)
            Result.failure(e)
        } catch (e: retrofit2.HttpException) {
            Log.e(TAG, "HTTP error while updating group: ${e.code()}", e)
            Result.failure(e)
        }
    }

    override suspend fun getMidpointByJoinCode(joinCode: String): Result<MidpointActivitiesResponse> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val response = groupInterface.getMidpointByJoinCode("Bearer $authToken", joinCode)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!) // Return GroupDataDetailed directly
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to fetch group by joinCode.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
        // Network or connection issues
        Result.failure(e)
        } catch (e: HttpException) {
            // Retrofit HTTP protocol issues (non-2xx responses)
            Result.failure(e)
        }
    }

    override suspend fun updateMidpointByJoinCode(joinCode: String): Result<MidpointActivitiesResponse> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val response = groupInterface.updateMidpointByJoinCode("Bearer $authToken", joinCode)
            
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!) // Return GroupDataDetailed directly
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to update midpoint by joinCode.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(IOException("Network error while updating midpoint: ${e.message}", e))
        } catch (e: HttpException) {
            Result.failure(HttpException(e.response() ?: throw e))
        } catch (e: JsonParseException) {
            Result.failure(JsonParseException("Error parsing server response: ${e.message}"))
        } catch (e: IllegalStateException) {
            Result.failure(IllegalStateException("Unexpected state while updating midpoint: ${e.message}", e))
        }
    }

    //activities

//    override suspend fun getActivities(joinCode: String): Result<List<Activity>> {
//        return try {
//            val response = activityInterface.getActivities("", joinCode)
//            val activities = response.body()?.data ?: emptyList()
//
//            Result.success(activities)
//        } catch (e: IOException) {
//            Log.e(TAG, "Error fetching activities", e)
//            Result.success(emptyList())
//        }
//    }


    override suspend fun selectActivity(joinCode: String, activity: Activity): Result<Unit> {
        return try {
            val request = SelectActivityRequest(joinCode, activity)
            val response = activityInterface.selectActivity(
                "", // Auth header handled by interceptor
                request
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to select activity: ${response.message()}")
                Result.failure(Exception("Failed to select activity"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error selecting activity", e)
            Result.failure(e)
        }
    }

    override suspend fun leaveGroup(joinCode: String, userId: String): Result<Unit> {
        return try {
            val authToken = tokenManager.getToken() ?: ""
            val request = LeaveGroupRequest(userId = userId)
    
            val response = groupInterface.leaveGroup(
                authHeader = "Bearer $authToken",
                joinCode = joinCode,
                request = request
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to leave group.")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}