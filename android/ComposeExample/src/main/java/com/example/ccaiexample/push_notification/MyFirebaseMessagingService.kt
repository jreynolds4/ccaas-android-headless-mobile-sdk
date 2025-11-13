package com.example.ccaiexample.push_notification

import android.content.Context
import android.app.ActivityManager
import android.util.Log
import com.ccaiplatform.ccaikit.CCAI
import com.ccaiplatform.ccaikit.models.logger.LogLevel
import com.ccaiplatform.ccaikit.services.toPushNotification
import com.ccaiplatform.ccaikit.util.logging.LoggingUtil
import com.ccaiplatform.ccaiui.CCAIUI
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Fetch the FCM token when the service is created and update the token in CCAI push notification service
        scope.launch {
            fetchToken()
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        scope.launch {
            Log.d("FirebaseMessage", "Received message: ${remoteMessage.data}")

            // 1. Convert the data map into the PushNotification data class
            val originalPushNotification = remoteMessage.data.toPushNotification()

            // 2. Use the copy() method to create a NEW object with the updated message
            val updatedPushNotification = originalPushNotification?.copy(
                message = "This is a custom message" // Only the 'message' property is changed
            )

            // Handle CCAI platform push notifications here
            if (isAppInForeground()) {
                // If the app is in the foreground, do nothing.
                // The user is actively using the app, so we don't display a notification.
                LoggingUtil.log("App is in Foreground. Ignoring push notification.")
            } else {
                LoggingUtil.log(message = "Message Data: ${updatedPushNotification?.message}")

                CCAI.pushNotificationService?.handlePushNotification(remoteMessage.data)
                CCAIUI.handlePushNotification(application, updatedPushNotification)
            }

        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    override fun onNewToken(token: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful || task.result == null) {
                    LoggingUtil.log("Couldn't get FCM token", LogLevel.WARN)
                    return@addOnCompleteListener
                }
                val refreshedToken = task.result
                LoggingUtil.log("FCM token updated: $refreshedToken")
                refreshedToken?.let {
                    CCAI.pushNotificationService?.updatePushToken(it)
                }
            }
    }

    private suspend fun fetchToken() {
        val token = try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            LoggingUtil.log("Failed to get FCM token", LogLevel.ERROR)
            null
        }
        if (!token.isNullOrEmpty()) {
            CCAI.pushNotificationService?.updatePushToken(token)
        }
    }

    // --- Utility Function to Check App State (Crucial for Background-Only logic) ---
    private fun isAppInForeground(): Boolean {

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses ?: return false

        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                // If it's your process and in the foreground, return true
                return processInfo.processName == applicationContext.packageName
            }
        }
        return false
    }


    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
