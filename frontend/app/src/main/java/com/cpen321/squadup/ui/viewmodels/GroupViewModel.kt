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
import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.remote.dto.GeoLocation
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.asStateFlow
import com.cpen321.squadup.data.remote.dto.SquadGoal
import com.cpen321.squadup.data.remote.dto.TransitType

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

    private val _midpoint = MutableStateFlow<SquadGoal?>(null)
    val midpoint: StateFlow<SquadGoal?> = _midpoint
    private val _isGroupLeft = MutableStateFlow(false)
    val isGroupLeft: StateFlow<Boolean> = _isGroupLeft
    private val _isCalculatingMidpoint = MutableStateFlow(false)
    val isCalculatingMidpoint: StateFlow<Boolean> = _isCalculatingMidpoint

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()


    fun createGroup(groupName: String, meetingTime: String, groupLeaderId: GroupUser, expectedPeople: Number, activityType: String, autoMidpoint: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingGroup = true, errorMessage = null)

            val result = groupRepository.createGroup(groupName, meetingTime, groupLeaderId, expectedPeople, activityType, autoMidpoint)
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
                        "expectedPeople" to (group?.group?.expectedPeople ?: ""),
                        "activityType" to (group?.group?.activityType ?: ""),
                        "autoMidpoint" to (group?.group?.autoMidpoint ?: "")
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

    fun getMidpoint(joinCode: String) {
        viewModelScope.launch {
            _isCalculatingMidpoint.value = true
            val result = groupRepository.getMidpointByJoinCode(joinCode)
            if (result.isSuccess) {
                _midpoint.value = result.getOrNull()?.midpoint
                _activities.value = result.getOrNull()?.activities ?: emptyList()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to fetch midpoint"
                Log.e(TAG, "Error fetching midpoint: $error")
            }
            _isCalculatingMidpoint.value = false
        }
    }

    fun updateMidpoint(joinCode: String) {
        viewModelScope.launch {
            _isCalculatingMidpoint.value = true
            val result = groupRepository.updateMidpointByJoinCode(joinCode)
            if (result.isSuccess) {
                _midpoint.value = result.getOrNull()?.midpoint
                _activities.value = result.getOrNull()?.activities ?: emptyList()
                Log.e(TAG, "Updated midpoint!")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to fetch midpoint"
                Log.e(TAG, "Error fetching midpoint: $error")
            }
            _isCalculatingMidpoint.value = false
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

    fun updateMember(
        joinCode: String,
        address: Address?,
        transitType: TransitType?,
        expectedPeople: Int,
        meetingTime: String,
        autoMidpoint: Boolean,
        activityType: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val fetchResult = groupRepository.getGroupByJoinCode(joinCode)
            if (fetchResult.isSuccess) {
                    val joinResult = groupRepository.updateGroupSettings(
                        joinCode = joinCode,
                        address = address,
                        transitType = transitType,
                        meetingTime = meetingTime,
                        expectedPeople = expectedPeople,
                        autoMidpoint = autoMidpoint,
                        activityType = activityType
                    )
                    if (joinResult.isSuccess) {
                        onSuccess("Successfully updated!")
                    } else {
                        val error = joinResult.exceptionOrNull()?.message ?: "Failed to update"
                        onError(error)
                    }
                    resetMidpoint()
            } else {
                val error = fetchResult.exceptionOrNull()?.message ?: "Failed to update member settings"
                onError(error)
            }
        }
    }

    fun resetGroupDeletedState() {
        _isGroupDeleted.value = false
    }

    fun resetGroupLeftState() {
        _isGroupLeft.value = false
    }

    fun resetMidpoint() {
        _midpoint.value = null
        _activities.value = emptyList()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}