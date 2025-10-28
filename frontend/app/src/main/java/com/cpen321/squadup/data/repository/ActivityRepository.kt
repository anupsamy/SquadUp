package com.cpen321.squadup.data.repository

import android.util.Log
import com.cpen321.squadup.data.remote.api.ActivityInterface
import com.cpen321.squadup.data.remote.api.SelectActivityRequest
import com.cpen321.squadup.data.remote.dto.Activity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityInterface: ActivityInterface
) {
    companion object {
        private const val TAG = "ActivityRepository"
    }

    suspend fun getActivities(): Result<List<Activity>?> {
        return try {
            val response = activityInterface.getActivities("") // Auth header handled by interceptor

            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data)
            } else {
                Log.e(TAG, "Failed to fetch activities: ${response.message()}")
                Result.failure(Exception("Failed to fetch activities"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching activities", e)
            Result.failure(e)
        }
    }

    suspend fun selectActivity(placeId: String): Result<Unit> {
        return try {
            val response = activityInterface.selectActivity(
                "", // Auth header handled by interceptor
                SelectActivityRequest(placeId)
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to select activity: ${response.message()}")
                Result.failure(Exception("Failed to select activity"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error selecting activity", e)
            Result.failure(e)
        }
    }
}