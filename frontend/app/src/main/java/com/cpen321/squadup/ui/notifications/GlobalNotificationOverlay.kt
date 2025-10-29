package com.cpen321.squadup.ui.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

@Composable
fun GlobalNotificationOverlay(
    notificationManager: NotificationManager,
    modifier: Modifier = Modifier
) {
    val currentNotification by notificationManager.currentNotification.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    var isManuallyDismissed by remember { mutableStateOf(false) }
    
    // Show notification when it changes
    LaunchedEffect(currentNotification) {
        if (currentNotification != null) {
            isVisible = true
            isManuallyDismissed = false
            // Auto-dismiss after 3 seconds
            delay(3000)
            if (!isManuallyDismissed) {
                isVisible = false
                notificationManager.clearNotification()
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier.zIndex(1000f) // High z-index to appear above everything
    ) {
        currentNotification?.let { notification ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Notification icon
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                Color.Red,
                                RoundedCornerShape(4.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Notification content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Dismiss button
                    TextButton(
                        onClick = {
                            isManuallyDismissed = true
                            isVisible = false
                            notificationManager.clearNotification()
                        }
                    ) {
                        Text(
                            text = "×",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
