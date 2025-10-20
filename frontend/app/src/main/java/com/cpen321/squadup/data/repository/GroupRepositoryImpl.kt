package com.cpen321.squadup.data.repository

import android.util.Log
import com.cpen321.squadup.data.remote.api.GroupInterface
import com.cpen321.squadup.data.remote.dto.CreateGroupRequest
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.utils.JsonUtils.parseErrorMessage
import javax.inject.Inject
import javax.inject.Singleton

import com.cpen321.squadup.data.remote.api.RetrofitClient

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupInterface: GroupInterface
) : GroupRepository {

    companion object {
        private const val TAG = "GroupRepositoryImpl"
    }

    override suspend fun createGroup(groupName: String, meetingTime: String, groupLeader: String, expectedPeople: Number): Result<GroupData> {
        return try {
            val request = GroupData(groupName = groupName, meetingTime = meetingTime, groupLeaderID = groupLeader, expectedPeople = expectedPeople)
            val response = groupInterface.createGroup("Bearer your-auth-token", request)

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorMessage = parseErrorMessage(errorBodyString, "Failed to create group.")
                Log.e(TAG, "Failed to create group: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating group", e)
            Result.failure(e)
        }
    }
}