package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch

import com.cpen321.squadup.data.remote.dto.GroupData
import com.cpen321.squadup.data.remote.dto.GroupsDataAll
import com.cpen321.squadup.data.remote.dto.GroupDataDetailed
import com.cpen321.squadup.data.remote.dto.GroupUser

import com.cpen321.squadup.data.repository.GroupRepository
import android.util.Log
import retrofit2.HttpException

data class MainUiState(
    val groups: List<GroupDataDetailed> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun setSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun fetchGroups() {
        viewModelScope.launch {
            val result = groupRepository.getGroups()
            if (result.isSuccess) {
                
                val groups = result.getOrNull() ?: emptyList()
                Log.d(TAG, "MainViewModel fetchGroups: ${groups}")
                _uiState.value = _uiState.value.copy(groups = groups)
            } else {
                val error = result.exceptionOrNull()
                _uiState.value = _uiState.value.copy(errorMessage = error?.message ?: "Failed to fetch groups")
            }
        }
    }

    fun fetchGroupByJoinCode(joinCode: String, onSuccess: (GroupDataDetailed) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = groupRepository.getGroupByJoinCode(joinCode)
            if (result.isSuccess) {
                val group = result.getOrNull()
                group?.let { onSuccess(it) }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to fetch group"
                onError(error)
            }
        }
    }

    fun checkGroupExists(
        joinCode: String,
        onSuccess: (GroupDataDetailed) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = groupRepository.getGroupByJoinCode(joinCode)
            if (result.isSuccess) {
                val group = result.getOrNull()
                if (group != null) {
                    onSuccess(group)
                } else {
                    onError("Group not found")
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to check group"
                onError(error)
            }
        }
    }

    fun joinGroup(
        joinCode: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val joinResult = groupRepository.joinGroup(
                joinCode = joinCode
            )
            Log.d(TAG, "MainViewModel joinResult: ${joinResult}")
            if (joinResult.isSuccess) {
                onSuccess("Successfully joined the group!")
                fetchGroups()
            } else {
                val error = when (val exception = joinResult.exceptionOrNull()) {
                    is HttpException -> {
                        when (exception.code()) {
                            404 -> "Group not found"
                            409 -> "You are already in this group"
                            500 -> "Failed to join group. Please try again"
                            else -> exception.message() ?: "Failed to join group"
                        }
                    }
                    else -> exception?.message ?: "Failed to join group"
                }
                onError(error)
            }
        }
    }
    
    fun getGroupById(groupId: String): GroupDataDetailed? {
        viewModelScope.launch {
            val result = groupRepository.getGroupByJoinCode(groupId)
            if (result.isSuccess) {
                val updatedGroup = result.getOrNull()
                updatedGroup?.let { newGroup ->
                    val updatedGroups = _uiState.value.groups.map { existingGroup ->
                        if (existingGroup.joinCode == groupId) newGroup else existingGroup
                    }

                    _uiState.value = _uiState.value.copy(
                        groups = updatedGroups,
                        successMessage = null,
                        errorMessage = null
                    )
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to fetch group"
                _uiState.value = _uiState.value.copy(errorMessage = error)
                Log.e(TAG, "MainViewModel fetchGroupByJoinCode error: $error")
            }
        }
        return _uiState.value.groups.find { it.joinCode == groupId } // Use joinCode directly
    }

    fun leaveGroup(
        joinCode: String,
        userId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = groupRepository.leaveGroup(joinCode, userId)
            Log.d(TAG, "MainViewModel leaveGroup result: ${result}")
            if (result.isSuccess) {
                onSuccess("Successfully left the group!")
                fetchGroups() // Refresh the group list
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to leave group"
                onError(error)
            }
        }
    }
}
