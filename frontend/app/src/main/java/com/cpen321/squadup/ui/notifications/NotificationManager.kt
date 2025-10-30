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
                "group_join" -> {
                    val notification = AppNotification(
                        id = System.currentTimeMillis().toString(),
                        title = "Group Member Joined",
                        message = messageText,
                        type = type,
                        timestamp = timestamp
                    )
                    showNotification(notification)
                }
                "group_leave" -> {
                    val notification = AppNotification(
                        id = System.currentTimeMillis().toString(),
                        title = "Group Member Left",
                        message = messageText,
                        type = type,
                        timestamp = timestamp
                    )
                    showNotification(notification)
                }
                "group_update" -> {
                    val notification = AppNotification(
                        id = System.currentTimeMillis().toString(),
                        title = "Group Update",
                        message = messageText,
                        type = type,
                        timestamp = timestamp
                    )
                    showNotification(notification)
                }
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
                    // For any other message type, still show it
                    val notification = AppNotification(
                        id = System.currentTimeMillis().toString(),
                        title = "Notification",
                        message = messageText,
                        type = type,
                        timestamp = timestamp
                    )
                    showNotification(notification)
                }
            }
        } catch (e: Exception) {
            // If not JSON, ignore or handle as needed
        }
    }
}
