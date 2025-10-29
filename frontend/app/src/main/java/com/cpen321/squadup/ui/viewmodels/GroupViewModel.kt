package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.cpen321.squadup.data.repository.GroupRepository
import com.cpen321.squadup.data.remote.dto.GroupUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.cpen321.squadup.data.remote.dto.SquadGoal

data class GroupUiState(
    val isCreatingGroup: Boolean = false,
    val successMessage: String? = null,
    val responseData: Map<String, Any>?= null,
    val errorMessage: String? = null
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {
    companion object {
        private const val TAG = "GroupViewModel"
    }
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState

    private val _isGroupDeleted = MutableStateFlow(false)
    val isGroupDeleted: StateFlow<Boolean> = _isGroupDeleted

<<<<<<< HEAD
    //data class Midpoint(val lat: Double, val lng: Double)
    private val _midpoint = MutableStateFlow<SquadGoal?>(null)
    val midpoint: StateFlow<SquadGoal?> = _midpoint
=======
    private val _isGroupLeft = MutableStateFlow(false)
    val isGroupLeft: StateFlow<Boolean> = _isGroupLeft
>>>>>>> main

    fun createGroup(groupName: String, meetingTime: String, groupLeaderId: GroupUser, expectedPeople: Number) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingGroup = true, errorMessage = null)

            val result = groupRepository.createGroup(groupName, meetingTime, groupLeaderId, expectedPeople)
            if (result.isSuccess) {
                val group = result.getOrNull()
                Log.d(TAG, "GroupViewModel createGroup: ${group}")
                _uiState.value = _uiState.value.copy(
                    isCreatingGroup = false,
                    successMessage = "Group '${groupName}' created successfully! Join Code: ${group?.group?.joinCode}",
                    responseData= mapOf(
                        "joinCode" to (group?.group?.joinCode ?: ""),
                        "groupName" to (group?.group?.groupName ?: ""),
                        "groupLeaderId" to (group?.group?.groupLeaderId ?: ""),
                        "meetingTime" to (group?.group?.meetingTime ?: ""),
                        "expectedPeople" to (group?.group?.expectedPeople ?: "")
                    )
                )
            } else {
                val error = result.exceptionOrNull()
                _uiState.value = _uiState.value.copy(
                    isCreatingGroup = false,
                    errorMessage = error?.message ?: "Failed to create group"
                )
            }
        }
    }

    fun deleteGroup(joinCode: String) {
        viewModelScope.launch {
            val result = groupRepository.deleteGroupByJoinCode(joinCode)
            if (result.isSuccess) {
                Log.d(TAG, "Group deleted successfully: $joinCode")
                _isGroupDeleted.value = true // Update the deletion state
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to delete group"
                Log.e(TAG, "Error deleting group: $error")
            }
        }
    }

    data class Midpoint(val lat: Double, val lng: Double)

    fun getMidpoint(joinCode: String) {
        viewModelScope.launch {
            val result = groupRepository.getMidpointByJoinCode(joinCode)
            if (result.isSuccess) {
                //val midpointValue = parseMidpointString(midpointString)
                _midpoint.value = result.getOrNull()
                //Log.d(TAG, "Parsed midpoint: $midpointValue")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to fetch midpoint"
                Log.e(TAG, "Error fetching midpoint: $error")
            }
        }
    }

    fun leaveGroup(joinCode: String, userId: String) {
        viewModelScope.launch {
            val result = groupRepository.leaveGroup(joinCode, userId)
            if (result.isSuccess) {
                Log.d(TAG, "Left group successfully: $joinCode")
                _isGroupLeft.value = true // Update the leave state
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to leave group"
                Log.e(TAG, "Error leaving group: $error")
                _uiState.value = _uiState.value.copy(errorMessage = error)
            }
        }
    }

<<<<<<< HEAD
    private fun parseMidpointString(midpointString: String): Midpoint? {
        return try {
            val parts = midpointString.trim().split("\\s+".toRegex())
            if (parts.size == 2) {
                val lat = parts[0].toDouble()
                val lng = parts[1].toDouble()
                Midpoint(lat, lng)
            } else {
                Log.e("GroupViewModel", "Unexpected midpoint format: $midpointString")
                null
            }
        } catch (e: Exception) {
            Log.e("GroupViewModel", "Failed to parse midpoint: $midpointString", e)
            null
        }
    }


=======
>>>>>>> main
    fun resetGroupDeletedState() {
        _isGroupDeleted.value = false
        _midpoint.value = null
    }

    fun resetGroupLeftState() {
        _isGroupLeft.value = false
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}