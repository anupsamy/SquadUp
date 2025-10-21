package com.cpen321.squadup.data.repository

import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupLeaderUser
import com.cpen321.squadup.data.remote.dto.GroupsDataAll

interface GroupRepository {
    suspend fun getGroups(): Result<GroupsDataAll>
    suspend fun createGroup(
        groupName: String, 
        meetingTime: String, 
        groupLeaderId: GroupLeaderUser, 
        expectedPeople: Number
    ): Result<GroupData>
}