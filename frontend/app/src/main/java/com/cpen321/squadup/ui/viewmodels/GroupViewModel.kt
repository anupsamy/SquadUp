package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.cpen321.squadup.data.repository.GroupRepository
import com.cpen321.squadup.data.remote.dto.GroupData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupUiState(
    val isCreatingGroup: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState

    fun createGroup(groupName: String, meetingTime: String, groupLeaderId: String, expectedPeople: Number) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingGroup = true, errorMessage = null)
    
            val result = groupRepository.createGroup(groupName = groupName, meetingTime = meetingTime, groupLeaderId = groupLeaderId, expectedPeople = expectedPeople)
            if (result.isSuccess) {
                val group = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    isCreatingGroup = false,
                    successMessage = "Group '${group?.groupName}' created successfully!"
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