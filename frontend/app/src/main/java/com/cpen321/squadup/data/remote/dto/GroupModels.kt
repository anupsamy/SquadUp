package com.cpen321.squadup.data.remote.dto

data class CreateGroupRequest(
    val groupName: String? = null,
    val meetingTime: String? = null,
    val groupLeaderId: String? = null,
    val expectedPeople: Number? = null
)

data class GroupData(
    val groupName: String,
    val meetingTime: String,
    val groupLeaderId: String,
    val expectedPeople: Number
)