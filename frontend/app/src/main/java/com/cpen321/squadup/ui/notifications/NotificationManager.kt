package com.cpen321.squadup.ui.notifications

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: String
)

@Singleton
class NotificationManager @Inject constructor() {
    
    private val _currentNotification = MutableStateFlow<AppNotification?>(null)
    val currentNotification: StateFlow<AppNotification?> = _currentNotification.asStateFlow()
    
    fun showNotification(notification: AppNotification) {
        _currentNotification.value = notification
    }
    
    fun clearNotification() {
        _currentNotification.value = null
    }
    
    fun handleWebSocketMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.optString("type", "unknown")
            val messageText = json.optString("message", message)
            val timestamp = json.optString("timestamp", "")
            
            when (type) {
                "notification" -> {
                    val notification = AppNotification(
                        id = System.currentTimeMillis().toString(),
                        title = "New Notification",
                        message = messageText,
                        type = type,
                        timestamp = timestamp
                    )
                    showNotification(notification)
                }
                "welcome" -> {
                    // Handle welcome message if needed
                }
                else -> {
                    // Handle other message types if needed
                }
            }
        } catch (e: Exception) {
            // If not JSON, ignore or handle as needed
        }
    }
}
