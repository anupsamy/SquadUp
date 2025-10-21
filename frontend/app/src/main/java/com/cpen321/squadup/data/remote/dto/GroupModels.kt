package com.cpen321.squadup.data.remote.dto

data class CreateGroupRequest(
    val groupName: String? = null,
    val meetingTime: String? = null,
    val groupLeaderId: GroupLeaderUser? = null,
    val expectedPeople: Number? = null
)

data class GroupsDataAll(
    val groups: List<GroupDataDetailed>
)
data class GroupData(
    val group: GroupDataDetailed
)
data class GroupDataDetailed(
    val groupName: String,
    val meetingTime: String,
    val joinCode: String,
    val groupLeaderId: GroupLeaderUser?,
    val expectedPeople: Number
)

data class GroupLeaderUser(
    val id: String,
    val name: String,
    val email: String
)