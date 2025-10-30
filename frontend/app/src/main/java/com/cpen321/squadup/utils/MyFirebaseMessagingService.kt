package com.cpen321.squadup.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.cpen321.squadup.MainActivity
import com.cpen321.squadup.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.ViewModelProvider
import com.cpen321.squadup.data.remote.dto.User
import androidx.lifecycle.ProcessLifecycleOwner

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // ---> Added: Log full FCM message for debugging
        Log.d("FCM", "Received FCM: ${remoteMessage.data} | ${remoteMessage.notification}")
        // <---

        val actingUserId = remoteMessage.data["actingUserId"]
        val currentUserId = getCurrentUserId(this)
        if (actingUserId != null && currentUserId != null && actingUserId == currentUserId) {
            Log.d(TAG, "Suppressing self-notification: actingUserId = $actingUserId (current user)")
            return // Don't display notification
        }

        // Handle data payload (quick tasks)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleNow()
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "Notification", it.body ?: "")
        }

        // Optional: simple notification for fallback
        val channelId = "default"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(remoteMessage.notification?.title ?: "Notification")
            .setContentText(remoteMessage.notification?.body ?: "")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        manager.notify(0, notification)
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement sending token to your server
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}

fun getCurrentUserId(context: Context): String? {
    // Safest cross-app way: store user id in shared preferences on profile load, then retrieve here
    // For this demo, let's try via ProfileViewModel, but this may need to be refactored in your real app for proper background access.
    // You may want to implement a singleton (object) or SharedPreference for this.
    return try {
        val profileViewModel = ViewModelProvider(ProcessLifecycleOwner.get()).get(ProfileViewModel::class.java)
        profileViewModel.uiState.value.user?._id
    } catch (e: Exception) {
        null
    }
}
