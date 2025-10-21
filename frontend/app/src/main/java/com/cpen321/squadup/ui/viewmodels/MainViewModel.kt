package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MainUiState(
    val groups: List<GroupData> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

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
            val result = groupRepository.getAllGroups()
            if (result.isSuccess) {
                val groups = result.getOrNull() ?: emptyList()
                _uiState.value = _uiState.value.copy(groups = groups)
            } else {
                val error = result.exceptionOrNull()
                _uiState.value = _uiState.value.copy(errorMessage = error?.message ?: "Failed to fetch groups")
            }
        }
    }
    
    fun getGroupById(groupId: String): GroupDataDetailed? {
        return _uiState.value.groups.find { it.group._id == groupId }?.group
    }
}
