package com.akustom15.crush.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.akustom15.crush.data.CrushPreferences
import com.google.firebase.messaging.FirebaseMessaging

object CrushNotificationHelper {

    private const val TAG = "CrushNotificationHelper"
    const val CHANNEL_ID = "crush_updates"
    const val TOPIC_UPDATES = "app_updates"

    fun initialize(context: Context) {
        Log.d(TAG, "initialize() called for package: ${context.packageName}")
        createNotificationChannel(context)
        syncSubscription(context)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "FCM token: ${task.result}")
            } else {
                Log.e(TAG, "Failed to get FCM token", task.exception)
            }
        }
        Log.d(TAG, "POST_NOTIFICATIONS permission: ${hasNotificationPermission(context)}")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Updates"
            val descriptionText = "Notifications for new versions and content"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                val soundResId = context.resources.getIdentifier(
                    "new_notification_011", "raw", context.packageName
                )
                if (soundResId != 0) {
                    val soundUri = Uri.parse(
                        "android.resource://${context.packageName}/$soundResId"
                    )
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                    Log.d(TAG, "Channel created with custom sound: new_notification_011")
                } else {
                    Log.w(TAG, "Custom sound not found, using default")
                }
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$CHANNEL_ID' created with importance HIGH")
        }
    }

    fun syncSubscription(context: Context) {
        val preferences = CrushPreferences.getInstance(context)
        val enabled = preferences.getNotificationsEnabled()

        if (enabled) {
            subscribeToUpdates()
        } else {
            unsubscribeFromUpdates()
        }
    }

    private fun subscribeToUpdates() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_UPDATES)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully subscribed to topic: $TOPIC_UPDATES")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to subscribe to topic: $TOPIC_UPDATES", e)
            }
    }

    private fun unsubscribeFromUpdates() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_UPDATES)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Unsubscribed from topic: $TOPIC_UPDATES")
                } else {
                    Log.e(TAG, "Failed to unsubscribe from topic: $TOPIC_UPDATES", task.exception)
                }
            }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
