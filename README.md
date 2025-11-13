# CCaaS Mobile SDK

## Setup Instructions

### Retrieve Company Credentials

1. Sign in to the Portal using admin credentials
1. Navigate to 'Settings > Developer Settings'
1. Under 'Company Key & Secret Code', note down your `Company Key` and `Company Secret Code`

### Run the Local Server

1. Copy `server/.env.example` to `.env` and add your `COMPANY_SECRET`
2. Start the local server:
   * Run `npm install` followed by `node app.js`
   * For physical device testing:
      * Create a tunnel using: `ssh -R 80:localhost:3000 ssh.localhost.run`
      * Replace the server URL with the tunnel URL from `AuthController.swift` file (e.g., `https://yourname.lhr.life`)

## Android

### Run Example App

1. Update `android/gradle/libs.versions.toml` with the following:

    ```toml
    [versions]
    ccaiVersion = "3.1.0"  # Replace with the latest CCAI SDK version

    [libraries]
    ccai-kit = { group = "com.ccaiplatform.android", name = "CCAIKit", version.ref = "ccaiVersion" }
    ccai-ui = { group = "com.ccaiplatform.android", name = "CCAIUI", version.ref = "ccaiVersion" }
    ccai-chat = { group = "com.ccaiplatform.android", name = "CCAIChat", version.ref = "ccaiVersion" }
    ccai-chat-red = { group = "com.ccaiplatform.android", name = "CCAIChatRed", version.ref = "ccaiVersion" }
    ccai-screenshare = { group = "com.ccaiplatform.android", name = "CCAIScreenShare", version.ref = "ccaiVersion" }
    ```

1. Configure environment files:
   * Rename `android/Shared/src/main/assets/environment.json.example` to `environment.json` and add your `key` and `hostname`
1. There are two apps, one is Jetpack Compose based (ComposeExample) and another is XML based (XMLExample). You can launch either of the Android apps
1. Configure the queue:
   * In Portal, go to 'Settings > Queue Menu'
   * Enable the chat channel
   * Copy the queue ID from the URL
   * Enter the queue ID in the Android app
   * You can also fetch the queue structure with API `CCAI.queueMenuService?.getMenus(null)`
1. To enable push notifications, refer to the [Setup Push Notifications](#setup-push-notifications) section

### Integrate to your project

#### Requirements

* Android 5.0 (API level 21, Lollipop) or later
* Firebase Cloud Messaging or Google Cloud Messaging for push notifications

#### Steps

1. Download the required Android packages by following these steps:

   * Update your root level `settings.gradle.kts` with the following:

     ```kotlin
     repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://sdk.ujet.co/ccaip/android/")
        }
     }
     ```

   * Or update your root level `android/build.gradle` with the following:

     ```groovy
     allprojects {
        repositories {
            google()
            mavenCentral()
            maven {
                url "https://sdk.ujet.co/ccaip/android/"
            }
        }
     }
     ```

   * Update your shared or app level `build.gradle.kts` with the following:

     ```kotlin
     dependencies {
        // Recommended: Use version catalog for consistent version management
        implementation(libs.ccai.kit)
        implementation(libs.ccai.chat)
        implementation(libs.ccai.chat.red)
        implementation(libs.ccai.ui) // Optional, only if you want to use CCAI push notifications UI
        implementation(libs.ccai.screenshare) // Optional, for screen share functionality
        
        // Alternative: Direct dependency declarations (not recommended for multi-module projects)
        val ccaiSdkVersion = "3.1.0"
        implementation("com.ccaiplatform.android:CCAIKit:$ccaiSdkVersion")
        implementation("com.ccaiplatform.android:CCAIChat:$ccaiSdkVersion")
        implementation("com.ccaiplatform.android:CCAIChatRed:$ccaiSdkVersion")
        implementation("com.ccaiplatform.android:CCAIUI:$ccaiSdkVersion") // Optional, only if you want to use CCAI push notifications UI
        implementation("com.ccaiplatform.android:CCAIScreenShare:$ccaiSdkVersion") // Optional, for screen share functionality
     }
     ```

1. Initialize SDK in Android's Application class `onCreate()` method. To initialize the CCAI SDK, you need to provide the `InitOptions` which includes the company key, host URL, listener and language code.

   ```kotlin
   class YourApplication : Application() {
      override fun onCreate() {
           // ...
           val languageCode = "en" // Replace with your desired language code
           val options = InitOptions(
             key = "your_company_key",
             urlHost = "your_host_url",
             ccaiListener = yourListener,
             languageCode = languageCode
           )
           try {
             CCAI.initialize(context, options)
             /** To initialize the optional service like chat service, you can call the `initializeChat()` 
              *  method with the context, `ChatOptions`, and language code. This will set up the chat  
              *  service and allow you to interact with it. After that, you can access the chat service 
              *  through the `CCAI.chatService` property. 
              */
             CCAI.initializeChat(context, ChatOptions(webFormInterface = WebFormManager()), languageCode)
             /** Initialize CCAIUI to support push notifications. To initialize the CCAIUI, you can  
              *  call the `initialize()` method with the context.
              */  
             CCAIUI.initialize(context)
           } catch (e: Exception) {
             Log.e("TAG", "Initialization failed with error: $e")
           }
      }
   }
   ```

1. Implement the `CCAIListener` to handle authentication. You can refer to `AuthController.kt` on how to sign JWT remotely.

   ```kotlin
   class YourAuthController : CCAIListener {
      override suspend fun ccaiShouldAuthenticate(): String? {
          // Sign JWT from your server and implement the logic to generate a JWT
          val jwt = signJWTForEndUser() ?: return null
          // Then authenticate JWT using `authService` to get an auth token
          return CCAI.authService?.authenticate(jwt)
      }
   }   
   ```

1. After initializing the SDK, you can access various services like `AuthService`, `CompanyService`, and `QueueMenuService` through the shared instance.

   ```kotlin
   val authService = CCAI.authService
   val companyService = CCAI.companyService
   val queueMenuService = CCAI.queueMenuService
   val pushNotificationService = CCAI.pushNotificationService
   // You can fetch the queue structure with following API
   val menus = queueMenuService?.getMenus(null)
   ```

1. Start chat by following the steps below and refer to `ChatViewModel.kt` for more details.

   ```kotlin
   val chatService = CCAI.chatService
   val menuId = 123 // Replace with actual menu ID
   chatService?.start(
       ChatRequest(
           chat = Chat(menuId),
           isScreenShareable = ScreenShareManager.isAvailable()
       )
   )
   ```

#### Setup Push Notifications

1. To enable push notifications, download the `google-service.json` file and place it inside of your project at the following location `${ROOT_ANDROID_PROJECT_PATH}/google-services.json`

1. Ensure that you have initialized the `CCAIUI` in your Application class as shown above

1. If you are using FCM, implement listener in your Firebase Messaging Service class for push notifications. If they are not implemented, the service will not work properly.

     ```kotlin
     class MyFirebaseMessagingService : FirebaseMessagingService() {
        // ...
        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            scope.launch {
                // Handle CCAI platform push notifications here
                CCAI.pushNotificationService?.handlePushNotification(remoteMessage.data)
                // Use the CCAIUI to let CCAI handle the push notification message UI. You can skip this 
                // and replace with your own push notification message UI by processing `remoteMessage.data`.
                CCAIUI.handlePushNotification(application, remoteMessage.data.toPushNotification())
            }
        }
     }
     ```

1. Also, make sure to implement the `onNewToken` method to handle token updates. This ensures that the CCAI SDK receives the latest push notification token.

   * To update the push notification token, call `CCAI.pushNotificationService?.updatePushToken(token)` and provide the new push token as an argument.

     ```kotlin
      class MyFirebaseMessagingService : FirebaseMessagingService() {
         // ...
         override fun onNewToken(token: String) {
             // Fetch the updated token from Firebase and update it in CCAI
             val updatedToken = token // Get the updated token from Firebase
             CCAI.pushNotificationService?.updatePushToken(token)
         }
      }
     ```

   * Update AndroidManifest.xml.

     ```xml
     <application>
     <service
       android:name=".firebase.MyFirebaseMessagingService"
       android:exported="true">
       <intent-filter>
         <action android:name="com.google.firebase.MESSAGING_EVENT" />
       </intent-filter>
     </service>
     </application>
     ```

### Screen Share Integration

Screen sharing allows agents to view and interact with the user's device screen during a chat session. Follow these steps to integrate screen sharing functionality:

1. **Initialize Screen Share Service**

   Initialize the screen share service in your Application class:

   ```kotlin
   val screenShareOptions = ScreenShareOptions(screenShareKey, screenShareDomain)
   CCAI.initializeScreenShare(context, screenShareOptions)
   ```

2. **Check Screen Share Eligibility**

   Before enabling screen share functionality, check if it's available for the current chat:

   ```kotlin
   fun isEnabled(chat: ChatResponse): Boolean {
       return chat.status.isAssigned() &&
           chat.supportScreenShare &&
           ScreenShareManager.isAvailable()
   }
   ```

3. **Handle Screen Share Requests**

   Implement screen share request handling in your ViewModel:

   **Client-Initiated Screen Share**

   ```kotlin
   // Direct screen share session start
   fun startScreenShareSession(chatId: String) {
       val createScreenShareCallbacks = ScreenShareCallbacks(
           onSessionStateChanged = { state ->
               Log.d("ScreenShare", "Screen share state changed: $state")
               // Update UI based on state changes
           },
           onSessionCreationError = { error ->
               Log.e("ScreenShare", "Session creation failed: ${error.message}")
               // Show error message to user
           },
           onSessionActivationRequest = {
               Log.d("ScreenShare", "Activation request received")
               // Auto-activate or show confirmation dialog
               ScreenShareManager.activateSession()
           },
           onSessionRemoteControlRequest = {
               Log.d("ScreenShare", "Remote control request received")
               // Enable/disable remote control based on user preference
               ScreenShareManager.enableRemoteControl(true) // or false
           },
           onSessionFullDeviceRequest = {
               Log.d("ScreenShare", "Full device request received")
               // Enable/disable full device sharing based on user preference
               ScreenShareManager.enableFullDeviceSharing(true) // or false
           },
           onSessionDidSucceed = { response ->
               Log.d("ScreenShare", "Session started successfully")
               // Show success message or update UI
           }
       )
       
       ScreenShareManager.startSession(
           ScreenShareRequest(
               communicationId = chatId,
               communicationType = CommunicationType.Chat,
               initiatedFrom = InitiatedFrom.EndUser
           ),
           callbacks = createScreenShareCallbacks
       )
   }
   ```

   **Agent-Initiated Screen Share**

   ```kotlin
   // Handle incoming screen share requests
   service?.messagesReceivedSubject?.collect { chatMessages ->
       // Check if there's an active screen share request from agent
       if (!chatMessages.findScreenShareRequest()) return
       // Process screen share request from agent
       ScreenShareManager.startSession(
           ScreenShareRequest(
               communicationId = chatId,
               communicationType = CommunicationType.Chat,
               initiatedFrom = ScreenShareFrom.AGENT
           ),
           callbacks = createScreenShareCallbacks
       )
   }

   fun List<ChatMessage>.findScreenShareRequest(): Boolean {
       return fold(false) { shouldShow, message ->
           when (message.body.event) {
               ChatMessageEvent.ScreenShareRequestedFromAgent -> true
               ChatMessageEvent.ScreenShareEnded -> false
               else -> shouldShow
           }
       }
   }
   ```

4. **Handle Screen Share State Changes**

   Listen to screen share session state changes:

   ```kotlin
   DisposableEffect(Unit) {
       val listener = { state: ScreenShareSessionState ->
           // Update UI based on screen share state
           // Example: Change status bar color to indicate active screen sharing
           val statusBarColor = if (state.isActive()) Color.Red else Color.Transparent
           systemUiController.setStatusBarColor(
               color = statusBarColor,
               darkIcons = true
           )
       }
       ScreenShareManager.addStateChangeListener(listener)
       onDispose {
           ScreenShareManager.removeStateChangeListener(listener)
       }
   }
   ```

   You can also get the current state directly:

   ```kotlin
   val state = ScreenShareManager.getSessionState()
   // Use state to check session status and update UI
   ```
