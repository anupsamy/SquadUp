package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.cpen321.squadup.data.repository.GroupRepository
import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupLeaderUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

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

    fun createGroup(groupName: String, meetingTime: String, groupLeaderId: GroupLeaderUser, expectedPeople: Number) {
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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}