package com.cpen321.squadup.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cpen321.squadup.ui.viewmodels.ChatViewModel
import com.cpen321.squadup.ui.components.NotificationOverlay
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ChatScreen(viewModel: com.cpen321.squadup.ui.viewmodels.ChatViewModel) {
    val messages = viewModel.messages.collectAsState()
    val isConnected = viewModel.isConnected.collectAsState()
    val currentNotification = viewModel.currentNotification.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Connection status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConnected.value) Color.Green.copy(alpha = 0.1f) 
                                       else Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = if (isConnected.value) "Connected" else "Disconnected",
                        color = if (isConnected.value) Color.Green else Color.Red,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Only show persistent messages (welcome, etc.) - not notifications
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages.value) { msg ->
                    Card(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Notification overlay at the top
        NotificationOverlay(
            notification = currentNotification.value,
            onDismiss = { viewModel.clearCurrentNotification() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}