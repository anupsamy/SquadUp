package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cpen321.squadup.data.remote.api.RetrofitClient
import com.cpen321.squadup.data.remote.dto.HobbyNews
import com.cpen321.squadup.data.remote.dto.NewsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.GsonBuilder

data class NewsUiState(
    val isLoading: Boolean = false,
    val jsonString: String = "",
    val error: String? = null,
    val newsData: List<HobbyNews> = emptyList()
)

class NewsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun fetchNews(hobbies: List<String>) {
        if (hobbies.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                newsData = emptyList(),
                jsonString = "",
                error = null
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val request = NewsRequest(hobbies = hobbies)
                val response = RetrofitClient.newsInterface.getNewsByHobbies(request)
                val jsonString = gson.toJson(response.results)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    newsData = response.results,
                    jsonString = jsonString
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearData() {
        _uiState.value = NewsUiState()
    }
}