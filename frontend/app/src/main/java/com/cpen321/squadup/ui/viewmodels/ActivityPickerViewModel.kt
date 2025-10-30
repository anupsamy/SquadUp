package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityPickerViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val _selectedActivityId = MutableStateFlow<String?>(null)
    val selectedActivityId: StateFlow<String?> = _selectedActivityId.asStateFlow()

    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setInitialSelectedActivity(activity: Activity?) {
        if (_selectedActivity.value == null && activity != null) {
            _selectedActivity.value = activity
            _selectedActivityId.value = activity.placeId
        }
    }

    fun loadActivities(joinCode: String) {
        viewModelScope.launch {
            val result = groupRepository.getActivities(joinCode)
            result.onSuccess { fetched ->
                _activities.value = fetched.ifEmpty {
                    getDefaultActivities()
                }
            }

        }
    }

    fun selectActivity(placeId: String) {
        _selectedActivityId.value = placeId
        _selectedActivity.value = _activities.value.find { it.placeId == placeId }
    }

    fun confirmSelection(joinCode: String) {
        val placeId = _selectedActivityId.value ?: return
        val selectedActivity = _activities.value.find { it.placeId == placeId } ?: return
        viewModelScope.launch {
            groupRepository.selectActivity(joinCode, selectedActivity)
                .onSuccess {
                    // Handle success - maybe clear selection or show confirmation
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to select activity"
                }
        }
    }

    fun clearActivities() {
        _activities.value = emptyList()
        _selectedActivity.value = null
        _selectedActivityId.value = null
    }

    private fun getDefaultActivities(): List<Activity> = listOf(
        Activity(
            name = "using defaults",
            placeId = "ChIJN1t_tDeuEmsRUsoyG83frY58",
            address = "5678 Oak St, Vancouver",
            rating = 4.7,
            userRatingsTotal = 512,
            priceLevel = 3,
            type = "restaurant",
            latitude = 49.2627,
            longitude = -123.1407,
            businessStatus = "OPERATIONAL",
            isOpenNow = true
        )
    )
}
