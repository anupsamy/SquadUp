package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.squadup.data.remote.dto.Activity
import com.cpen321.squadup.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ActivityPickerViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val _selectedActivityId = MutableStateFlow<String?>(null)
    val selectedActivityId: StateFlow<String?> = _selectedActivityId.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            val result = repository.getActivities()
            result
                .onSuccess { fetched ->
                    _activities.value = fetched ?: emptyList()
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to load activities"
                }
        }
    }

    fun selectActivity(placeId: String) {
        _selectedActivityId.value = placeId
    }
}

//
//Activity(
//name = "Sushi Palace",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY58",
//address = "5678 Oak St, Vancouver",
//rating = 4.7,
//userRatingsTotal = 512,
//priceLevel = 3,
//type = "restaurant",
//latitude = 49.2627,
//longitude = -123.1407,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//),
//Activity(
//name = "Pizza Garden",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY47",
//address = "1234 Main St, Vancouver",
//rating = 4.3,
//userRatingsTotal = 256,
//priceLevel = 2,
//type = "restaurant",
//latitude = 49.2827,
//longitude = -123.1207,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//),
//Activity(
//name = "Pizza Garden",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY46",
//address = "1234 Main St, Vancouver",
//rating = 4.3,
//userRatingsTotal = 256,
//priceLevel = 2,
//type = "restaurant",
//latitude = 49.2827,
//longitude = -123.1207,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//),
//Activity(
//name = "Pizza Garden",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY45",
//address = "1234 Main St, Vancouver",
//rating = 4.3,
//userRatingsTotal = 256,
//priceLevel = 2,
//type = "restaurant",
//latitude = 49.2827,
//longitude = -123.1207,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//),
//Activity(
//name = "Pizza Garden",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY44",
//address = "1234 Main St, Vancouver",
//rating = 4.3,
//userRatingsTotal = 256,
//priceLevel = 2,
//type = "restaurant",
//latitude = 49.2827,
//longitude = -123.1207,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//),
//Activity(
//name = "Pizza Garden",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY43",
//address = "1234 Main St, Vancouver",
//rating = 4.3,
//userRatingsTotal = 256,
//priceLevel = 2,
//type = "restaurant",
//latitude = 49.2827,
//longitude = -123.1207,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//),
//Activity(
//name = "Pizza Garden",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY42",
//address = "1234 Main St, Vancouver",
//rating = 4.3,
//userRatingsTotal = 256,
//priceLevel = 2,
//type = "restaurant",
//latitude = 49.2827,
//longitude = -123.1207,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//),
//Activity(
//name = "Pizza Garden",
//placeId = "ChIJN1t_tDeuEmsRUsoyG83frY41",
//address = "1234 Main St, Vancouver",
//rating = 4.3,
//userRatingsTotal = 256,
//priceLevel = 2,
//type = "restaurant",
//latitude = 49.2827,
//longitude = -123.1207,
//businessStatus = "OPERATIONAL",
//isOpenNow = true
//)