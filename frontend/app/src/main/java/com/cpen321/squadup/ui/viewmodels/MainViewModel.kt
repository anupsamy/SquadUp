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
import com.cpen321.squadup.data.remote.dto.GroupLeaderUser

import com.cpen321.squadup.data.repository.GroupRepository
import android.util.Log

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
    
    fun getGroupById(groupId: String): GroupDataDetailed? {
        return _uiState.value.groups.find { it.joinCode == groupId } // Use joinCode directly
    }
}
