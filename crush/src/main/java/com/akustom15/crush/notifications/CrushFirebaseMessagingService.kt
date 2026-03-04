package com.akustom15.crush.notifications

import android.app.PendingIntent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.akustom15.crush.data.CrushPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CrushFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "CrushFCMService"
        private const val NOTIFICATION_ID = 3001
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: from=${message.from}, notification=${message.notification != null}, data=${message.data}")

        val preferences = CrushPreferences.getInstance(applicationContext)
        if (!preferences.getNotificationsEnabled()) {
            Log.d(TAG, "Notifications disabled by user, skipping")
            return
        }

        val title = message.notification?.title ?: message.data["title"]
        if (title == null) {
            Log.w(TAG, "No title found in message, skipping")
            return
        }
        val body = message.notification?.body ?: message.data["body"] ?: ""

        Log.d(TAG, "Showing notification: title=$title")
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                this,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            null
        }

        val notification = NotificationCompat.Builder(this, CrushNotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notification posted successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to show notification: POST_NOTIFICATIONS permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }
}
