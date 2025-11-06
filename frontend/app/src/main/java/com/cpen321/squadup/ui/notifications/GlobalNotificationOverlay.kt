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
private fun NotificationIcon() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(
                Color.Red,
                RoundedCornerShape(4.dp)
            )
    )
}

@Composable
private fun NotificationContent(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun DismissButton(
    onDismiss: () -> Unit
) {
    TextButton(onClick = onDismiss) {
        Text(
            text = "×",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun NotificationCard(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
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
            NotificationIcon()
            Spacer(modifier = Modifier.width(12.dp))
            NotificationContent(
                title = title,
                message = message
            )
            DismissButton(onDismiss = onDismiss)
        }
    }
}

@Composable
fun GlobalNotificationOverlay(
    notificationManager: NotificationManager,
    modifier: Modifier = Modifier
) {
    val currentNotification by notificationManager.currentNotification.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    var isManuallyDismissed by remember { mutableStateOf(false) }
    
    val handleDismiss = {
        isManuallyDismissed = true
        isVisible = false
        notificationManager.clearNotification()
    }
    
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
        modifier = modifier.zIndex(1000f)
    ) {
        currentNotification?.let { notification ->
            NotificationCard(
                title = notification.title,
                message = notification.message,
                onDismiss = handleDismiss
            )
        }
    }
}
