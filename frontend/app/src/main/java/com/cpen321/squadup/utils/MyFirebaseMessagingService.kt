package com.cpen321.squadup.utils

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cpen321.squadup.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

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

        // If app is in foreground, suppress showing a system notification.
        if (isAppInForeground()) {
            Log.d(TAG, "App in foreground; suppressing FCM notification UI")
            // Still allow any lightweight data processing if needed
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Message data payload (foreground): ${remoteMessage.data}")
                handleNow()
            }
            return
        }

        // App is background: show system notification if provided
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload (background): ${remoteMessage.data}")
        }
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "Notification", it.body ?: "")
        }
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement sending token to your server
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat
                .Builder(this, channelId)
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

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val myPackage = packageName
        for (appProcess in appProcesses) {
            if (appProcess.processName == myPackage &&
                appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            ) {
                return true
            }
        }
        return false
    }
}

fun getCurrentUserId(context: Context): String? {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_id", null)
}
