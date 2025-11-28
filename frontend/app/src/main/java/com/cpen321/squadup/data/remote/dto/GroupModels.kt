package com.cpen321.squadup.data.remote.dto

data class CreateGroupRequest(
    val groupName: String? = null,
    val meetingTime: String? = null,
    val groupLeaderId: GroupUser? = null,
    val expectedPeople: Number? = null,
    val activityType: String? = null,
    val autoMidpoint: Boolean? = null
)

data class UpdateGroupRequest(
    val joinCode: String,
    val address: Address?,
    val transitType: TransitType?,
    val meetingTime: String?,
    val expectedPeople: Number?,
    val autoMidpoint: Boolean? = null,
    val activityType: String? = null
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
    val groupLeaderId: GroupUser? = null,
    val expectedPeople: Number,
    val groupMemberIds: List<GroupUser>? = null,
    val midpoint: String? = null,
    val selectedActivity: Activity? = null,
    val activityType: String,
    val autoMidpoint: Boolean,
    val activities: List<Activity>? = null
)

data class GroupUser(
    val id: String,
    val name: String,
    val email: String,
    val address: Address? = null,
    val transitType: TransitType ?= null,
    val travelTime: String ?= null
)

data class ActivityCoordinates (
    val latitude: Double,
    val longitude: Double
)
