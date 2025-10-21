package com.cpen321.squadup.data.repository

import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupLeaderUser

interface GroupRepository {
    suspend fun createGroup(
        groupName: String, 
        meetingTime: String, 
        groupLeaderId: GroupLeaderUser, 
        expectedPeople: Number
    ): Result<GroupData>
}