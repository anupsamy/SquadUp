package com.cpen321.squadup.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

data class NotificationMessage(
    val id: String,
    val message: String,
    val type: String,
    val timestamp: String
)

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    // Expose a read-only StateFlow to Compose
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    // For temporary notifications
    private val _currentNotification = MutableStateFlow<NotificationMessage?>(null)
    val currentNotification: StateFlow<NotificationMessage?> = _currentNotification.asStateFlow()

    // Called when new websocket message arrives
    fun onNewMessage(text: String) {
        try {
            val json = JSONObject(text)
            val type = json.optString("type", "unknown")
            val message = json.optString("message", text)
            val timestamp = json.optString("timestamp", "")

            when (type) {
                "notification" -> {
                    // Show temporary notification
                    val notification = NotificationMessage(
                        id = System.currentTimeMillis().toString(),
                        message = message,
                        type = type,
                        timestamp = timestamp
                    )
                    _currentNotification.value = notification
                }
                "welcome" -> {
                    // Add welcome message to persistent list
                    _messages.value = _messages.value + text
                }
                else -> {
                    // Add other messages to persistent list
                    _messages.value = _messages.value + text
                }
            }
        } catch (e: JSONException) {
            // If not JSON, treat as regular message
            _messages.value = _messages.value + text
        }
    }

    // Called when connection state changes
    fun onConnectionStateChanged(connected: Boolean) {
        _isConnected.value = connected
    }

    // Clear current notification
    fun clearCurrentNotification() {
        _currentNotification.value = null
    }

    // Optional: a method to clear or mark messages handled
    fun clearMessages() {
        _messages.value = emptyList()
    }
}