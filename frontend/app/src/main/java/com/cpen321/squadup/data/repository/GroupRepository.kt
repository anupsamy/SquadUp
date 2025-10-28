package com.cpen321.squadup.data.repository

import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.GroupUser
import com.cpen321.squadup.data.remote.dto.GroupsDataAll

interface GroupRepository {
    suspend fun getGroups(): Result<List<GroupDataDetailed>>
    suspend fun createGroup(
        groupName: String, 
        meetingTime: String, 
        groupLeaderId: GroupUser, 
        expectedPeople: Number
    ): Result<GroupData>
    suspend fun getGroupByJoinCode(joinCode: String): Result<GroupDataDetailed>
    suspend fun deleteGroupByJoinCode(joinCode:String): Result<Unit>
    suspend fun joinGroup(
        joinCode: String, 
        expectedPeople: Number, 
        updatedMembers: List<GroupUser>
    ): Result<Unit> //add exp members later


}