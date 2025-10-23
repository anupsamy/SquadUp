package com.cpen321.squadup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cpen321.squadup.ui.navigation.AppNavigation
import com.cpen321.squadup.ui.screens.ChatScreen
import com.cpen321.squadup.ui.theme.ProvideFontSizes
import com.cpen321.squadup.ui.theme.ProvideSpacing
import com.cpen321.squadup.ui.theme.UserManagementTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.messaging.FirebaseMessaging
import com.cpen321.squadup.utils.WebSocketManager
import com.cpen321.squadup.ui.viewmodels.ChatViewModel
import com.cpen321.squadup.ui.notifications.NotificationManager
import com.cpen321.squadup.ui.notifications.GlobalNotificationOverlay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()
    
    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var wsManager: WebSocketManager

    // Launcher for requesting POST_NOTIFICATIONS permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        } else {
            Log.d("FCM", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ask notification permission on Android 13+
        askNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
        }

        // Initialize WebSocketManager
        // For local testing, use: ws://10.0.2.2:8080 (Android emulator)
        // For physical device, use your computer's IP: ws://192.168.1.xxx:8080
        // For online testing: wss://echo.websocket.org/
        wsManager = WebSocketManager("ws://10.0.2.2:8080")

        // Set listener callback to handle incoming messages
        wsManager.setListener(object : WebSocketManager.WebSocketListenerCallback {
            override fun onMessageReceived(message: String) {
                Log.d("WebSocket", "Received message: $message")
                
                // Handle notifications through the notification manager
                notificationManager.handleWebSocketMessage(message)
                
                // Also pass to chat view model for testing
                chatViewModel.onNewMessage(message)
            }

            override fun onConnectionStateChanged(isConnected: Boolean) {
                Log.d("WebSocket", "Connection state changed: $isConnected")
                chatViewModel.onConnectionStateChanged(isConnected)
            }
        })

        // Start connection
        wsManager.start()

        setContent {
            UserManagementTheme {
                UserManagementApp()
                
                // Global notification overlay - shows notifications from WebSocket
                GlobalNotificationOverlay(notificationManager = notificationManager)
                
                // Uncomment the line below to show ChatScreen for testing WebSocket notifications
                // ChatScreen(viewModel = chatViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanly close the websocket when activity is destroyed
        try {
            wsManager.stop()
        } catch (e: Exception) {
            Log.w("WebSocket", "Error stopping websocket: ${e.message}")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    Log.d("FCM", "Notification permission already granted")
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    // Directly request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
fun UserManagementApp() {
    ProvideSpacing {
        ProvideFontSizes {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                AppNavigation()
            }
        }
    }
}
