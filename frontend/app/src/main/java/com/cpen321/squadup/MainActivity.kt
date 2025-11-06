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
import java.io.IOException



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
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
            } else {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
            }
        }

        // Initialize WebSocketManager
        // For local testing, use: ws://10.0.2.2:3000/ws (Android emulator)
        // For AWS staging, use: ws://ec2-18-221-196-3.us-east-2.compute.amazonaws.com:80/ws
        val wsUrl = try {
            if (BuildConfig.FLAVOR == "staging") {
                "ws://ec2-18-221-196-3.us-east-2.compute.amazonaws.com:80/ws"
            } else {
                "ws://10.0.2.2:3000/ws" // Local development
            }
        } catch (e: NoSuchFieldError) {
            Log.w("WebSocket", "Error reading BuildConfig.FLAVOR: ${e.message}, defaulting to local")
            "ws://10.0.2.2:3000/ws"
        } catch (e: RuntimeException) {
            Log.w("WebSocket", "Error reading BuildConfig.FLAVOR: ${e.message}, defaulting to local")
            "ws://10.0.2.2:3000/ws"
        }
        Log.d("WebSocket", "Will connect to: $wsUrl")
        
        setContent {
            UserManagementTheme {
                UserManagementApp()
                
                // Global notification overlay - shows notifications from WebSocket
                GlobalNotificationOverlay(notificationManager = notificationManager)
            }
        }
        
        // Initialize WebSocket AFTER UI is set up to avoid blocking onCreate
        // Post to main thread to ensure UI is rendered first
        window.decorView.post {
            try {
                wsManager = WebSocketManager(wsUrl)
                
                // Set listener callback to handle incoming messages
                wsManager.setListener(object : WebSocketManager.WebSocketListenerCallback {
                    override fun onMessageReceived(message: String) {
                        Log.d("WebSocket", "Received message: $message")
                        
                        // Handle notifications through the notification manager
                        try {
                            if (::notificationManager.isInitialized) {
                                notificationManager.handleWebSocketMessage(message)
                            }
                        } catch (e: Exception) {
                            Log.e("WebSocket", "Error handling notification: ${e.message}")
                        }
                        
                        // Also pass to chat view model for testing
                        chatViewModel.onNewMessage(message)
                    }

                    override fun onConnectionStateChanged(isConnected: Boolean) {
                        Log.d("WebSocket", "Connection state changed: $isConnected")
                        if (!isConnected) {
                            Log.e("WebSocket", "WebSocket connection failed. Check:")
                            Log.e("WebSocket", "1. AWS server is running")
                            Log.e("WebSocket", "2. Security groups allow port 80")
                            Log.e("WebSocket", "3. WebSocket service is active on server")
                        }
                        chatViewModel.onConnectionStateChanged(isConnected)
                    }
                })

                // Start connection
                Log.d("WebSocket", "Starting WebSocket connection...")
                wsManager.start()
            } catch (e: Exception) {
                Log.e("WebSocket", "Error initializing WebSocket: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanly close the websocket when activity is destroyed
        try {
            if (::wsManager.isInitialized) {
                wsManager.stop()
            }
        } catch (e: IOException) {
            Log.w("WebSocket", "Error stopping websocket: ${e.message}")
        } catch (e: Exception) {
            Log.w("WebSocket", "Error in onDestroy: ${e.message}")
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
