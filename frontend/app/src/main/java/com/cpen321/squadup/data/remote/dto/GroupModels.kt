package com.cpen321.squadup.data.remote.dto

data class CreateGroupRequest(
    val groupName: String? = null,
    val meetingTime: String? = null,
    val groupLeaderId: GroupUser? = null,
    val expectedPeople: Number? = null
)

data class UpdateGroupRequest(
    val joinCode: String? = null,
    val expectedPeople: Number? = null,
    val groupMemberIds: List<GroupUser>?= null
)

data class LeaveGroupRequest(
    val userId: String
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
    val groupLeaderId: GroupUser?,
    val expectedPeople: Number,
    val groupMemberIds: List<GroupUser>?
)

data class GroupUser(
    val id: String,
    val name: String,
    val email: String
)

data class ActivityCoordinates (
    val latitude: Double,
    val longitude: Double
)
