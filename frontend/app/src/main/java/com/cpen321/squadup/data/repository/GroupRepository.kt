package com.cpen321.squadup.data.repository

import com.cpen321.squadup.data.remote.dto.GroupData

interface GroupRepository {
    suspend fun createGroup(groupName: String, meetingTime: String, groupLeaderId: String, expectedPeople: Number): Result<GroupData>
}