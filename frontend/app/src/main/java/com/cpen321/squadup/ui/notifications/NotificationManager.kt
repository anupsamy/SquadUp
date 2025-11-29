package com.cpen321.squadup.ui.notifications

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: String,
)

@Singleton
class NotificationManager
    @Inject
    constructor() {
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

                fun notify(
                    title: String,
                    msg: String,
                    notifType: String = type,
                ) {
                    val notification =
                        AppNotification(
                            id = System.currentTimeMillis().toString(),
                            title = title,
                            message = msg,
                            type = notifType,
                            timestamp = timestamp,
                        )
                    showNotification(notification)
                }

                when (type) {
                    "group_join" -> notify("Group Member Joined", messageText)
                    "group_leave" -> notify("Group Member Left", messageText)
                    "group_update" -> {
                        val dataObj = json.optJSONObject("data")
                        val updateType = dataObj?.optString("type", "")
                        if (updateType == "activity_selected") {
                            val activityObj = dataObj.optJSONObject("activity")
                            val activityName = activityObj?.optString("name", "an activity") ?: "an activity"
                            notify("Activity Selected", "Group leader selected \"$activityName\"", "activity_selected")
                        } else {
                            notify("Group Update", messageText)
                        }
                    }
                    "notification" -> notify("New Notification", messageText)
                    "welcome" -> {
                        // Handle welcome message if needed
                    }
                    else -> notify("Notification", messageText)
                }
            } catch (e: JSONException) {
                // If not JSON, ignore or handle as needed
            }
        }
    }
