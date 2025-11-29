package com.cpen321.squadup.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.squadup.data.remote.dto.Address
import com.cpen321.squadup.data.repository.PlacesRepository
import com.google.android.libraries.places.api.model.AutocompletePrediction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressPickerViewModel
    @Inject
    constructor(
        private val placesRepository: PlacesRepository,
    ) : ViewModel() {
        // User input in the TextField
        var query by mutableStateOf("")

        // Autocomplete predictions from Google Places
        var predictions by mutableStateOf<List<AutocompletePrediction>>(emptyList())
            private set

        // Selected address
        var selectedAddress by mutableStateOf<Address?>(null)

        // Job for debouncing input
        private var searchJob: Job? = null

        /**
         * Called when the user types in the TextField
         */
        fun onQueryChanged(newQuery: String) {
            query = newQuery
            selectedAddress = null // reset selection if user changes text

            // Debounce the API call
            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    delay(300) // 300ms debounce
                    predictions =
                        if (newQuery.isNotBlank()) {
                            placesRepository.getPredictions(newQuery)
                        } else {
                            emptyList()
                        }
                }
        }

        /**
         * Called when the user selects a prediction
         */
        suspend fun fetchPlace(placeId: String): Address? = placesRepository.fetchPlace(placeId)

        /**
         * Optional: clear the current selection and query
         */
        fun clear() {
            query = ""
            selectedAddress = null
            predictions = emptyList()
        }
    }
