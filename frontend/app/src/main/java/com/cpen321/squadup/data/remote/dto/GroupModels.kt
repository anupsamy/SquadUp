package com.cpen321.squadup.data.remote.dto

data class CreateGroupRequest(
    val name: String,
    val description: String
)

data class GroupData(
    val groupName: String,
    val meetingTime: String,
    val groupLeaderID: String,
    val expectedPeople: Number
)