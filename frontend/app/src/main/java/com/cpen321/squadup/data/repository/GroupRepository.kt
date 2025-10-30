package com.cpen321.squadup.data.repository

import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.GroupUser
import com.cpen321.squadup.data.remote.dto.MidpointActivitiesResponse
import com.cpen321.squadup.data.remote.dto.SquadGoal
import com.google.android.gms.maps.model.LatLng

interface GroupRepository {
    suspend fun getGroups(): Result<List<GroupDataDetailed>>
    suspend fun createGroup(
        groupName: String,
        meetingTime: String,
        groupLeaderId: GroupUser,
        expectedPeople: Number,
        activityType: String
    ): Result<GroupData>
    suspend fun getGroupByJoinCode(joinCode: String): Result<GroupDataDetailed>
    suspend fun deleteGroupByJoinCode(joinCode:String): Result<Unit>
    suspend fun joinGroup(
        joinCode: String,
        expectedPeople: Number,
        updatedMembers: List<GroupUser>,
    ): Result<Unit> //add exp members later
    suspend fun updateGroup(
        joinCode: String,
        expectedPeople: Number?,
        updatedMembers: List<GroupUser>?,
        meetingTime: String?
    ): Result<Unit>
    suspend fun getMidpointByJoinCode(joinCode: String): Result<MidpointActivitiesResponse>

    suspend fun leaveGroup(
        joinCode: String, 
        userId: String
    ): Result<Unit>
    suspend fun getActivities(joinCode: String): Result<List<Activity>>

    suspend fun selectActivity(joinCode: String, activity: Activity): Result<Unit>

    suspend fun getMidpoints(joinCode: String): Result<List<LatLng>>
}
