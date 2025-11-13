package com.example.xml_example.push_notification

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
            // Handle CCAI platform push notifications here
            CCAI.pushNotificationService?.handlePushNotification(remoteMessage.data)
            CCAIUI.handlePushNotification(application, remoteMessage.data.toPushNotification())
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

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
