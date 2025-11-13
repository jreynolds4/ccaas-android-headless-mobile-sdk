# CCAI Mobile SDK

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

## iOS

### Run Example App

1. Configure environment files:
   * Rename `ios/CCAIExample/Common/Resources/environment.json.example` to `environment.json` and add your `key` and `hostname`
1. Launch the iOS app
1. Configure the queue:
   * In Portal, go to 'Settings > Queue Menu'
   * Enable the chat channel
   * Copy the numeric queue ID from the URL
   * Enter the queue ID in the iOS app
   * You can also fetch the queue structure with API `CCAI.shared.queueMenuService?.get()`

### Integrate to your project

#### Requirements

* iOS 16.0+
* Swift Project

#### Steps

1. Add the following to your `Package.swift` file:

    ```swift
    dependencies: [
        .package(url: "https://github.com/UJET/ujet-ios-sdk-sp.git", branch: "3.1.0")
    ],
    targets: [
        .target(
            name: "YourTargetName",
            dependencies: [
                .product(name: "CCAIKit", package: "CCAIKit")
            ]
        )
    ]
    ```

1. Import `CCAIKit` in your `AppDelegate` file:

    ```swift
    import CCAIKit
    ```

    If your project is SwiftUI, then declare `@UIApplicationDelegateAdaptor` in your `App` struct:

    ```swift
    @main
    struct YourAppName: App {
        @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
        // ...
    }
    ```

1. Implement delegates for push notifications. If they are not implemented, the service will not work properly.

   ```swift
   import CCAIKit

   class AppDelegate: NSObject, UIApplicationDelegate {
      func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
         CCAI.shared.pushNotificationService?.updatePushToken(data: deviceToken, type: .apns)
      }

      func application(_ application: UIApplication,
                       didFailToRegisterForRemoteNotificationsWithError error: Error) {
         CCAI.shared.pushNotificationService?.updatePushToken(data: nil, type: .apns)
      }
   }
   ```

1. Enable Capabilities > Push Notifications and Background Mode for Remote notifications from Target > Signing & Capabilities tab.

1. Initialize SDK

   ```swift
   import CCAIKit

   let options = InitOptions(key: "company_key", urlHost: "your_subdomain.ccaiplatform.com", delegate: delegate, cacheAuthToken: false)

   do {
      try CCAI.shared.initialize(options: options)
      CCAI.shared.initializeChat()
   } catch {
      print("Failed to initialize CCAI: \(error.localizedDescription)")
   }
   ```

   The delegate should implement `CCAIDelegate` protocol.

   ```swift
   class AppDelegate: CCAIDelegate {
      func ccaiShouldAuthenticate() async -> String? {
         // First, sign JWT remotely
         guard let jwt = await signJWT() else { return nil }

         // Then authenticate JWT using `authService` to get an auth token
         return try? await CCAI.shared.authService?.authenticate(jwt)
      }
   }
   ```

   Please refer to `AuthController.swift` how to sign JWT remotely.

1. Start chat

   ```swift
   import CCAIKit
   import CCAIChat

   let chatService = CCAI.shared.chatService
   let request = ChatRequest(menuId: 123, isScreenShareable: screenShareService != nil)
   do {
      try await chatService?.start(request: request)
   } catch {
      print("Failed to start chat: \(error.localizedDescription)")
   }
   ```

   Please refer to `ChatViewModel.swift` for more details.

### Screen Share Integration

Screen sharing allows agents to view and interact with the user's device screen during a chat session. Follow these steps to integrate screen sharing functionality:

> **Prerequisites:** Add the `screenShareKey` to your `environment.json` file

1. Initialize Screen Share Service

   Initialize the screen share service in your Application class:

   ```swift
   CCAI.shared.initializeScreenShare(ScreenShareOptions(key: screenShareKey))
   ```

2. Check Screen Share Eligibility

   Before enabling screen share functionality, check if it's available for the current chat:

   ```swift
   let isChatSupportingScreenShare = screenShareService != nil && chat?.isSupportingScreenShare == true
   ```

3. Handle Screen Share Requests

   Implement screen share request handling in your ViewModel:

   ```swift
   func requestScreenShare(isFromRemote: Bool) async {
      guard let chatId = currentChatId else { return }
      guard let screenShareService = screenShareService else {
         handleError(ScreenShareError.serviceNotAvailable, message: "Request Screen Share")
         return
      }

      // Track whether this session is agent-initiated
      screenShareInitiatedFrom = isFromRemote ? .agent : .endUser

      if !isFromRemote {
         await sendScreenShareMessage(event: .screenShareRequestedFromEndUser)
      }

      do {
         let response = try await screenShareService.create(request: ScreenShareRequest(
               communicationId: chatId,
               communicationType: .chat,
               initiatedFrom: isFromRemote ? .agent : .endUser
         ))

         await sendScreenShareMessage(event: .screenShareCodeGenerated, response: response)
      } catch {
         await sendScreenShareMessage(event: .screenShareFailed)
         handleError(error, message: "Request screen share error")
      }
   }
   ```

4. Handle Screen Share State Changes

   Listen to screen share session state changes:

   ```swift
   // Set delegate to handle screen share events
   self.screenShareService?.delegate = self

   extension CCAIChatViewModel: @preconcurrency ScreenShareServiceDelegate {
      func screenShareService(_ service: any CCAIKit.ScreenShareServiceProtocol, didChangeState state: CCAIKit.ScreenShareServiceState) {
         currentScreenShareSessionState = state
         // For agent-initiated sessions, start the session immediately in `authorizing` state
         if state == .authorizing, screenShareInitiatedFrom == .agent {
               Task {
                  do {
                     try await handleStartSession(isAccepted: true, service: service)
                  } catch {
                     handleError(error, message: "Failed to start agent-initiated screen share session")
                  }
               }
         }
      }

      func screenShareService(_ service: any CCAIKit.ScreenShareServiceProtocol, didReceiveRequest request: CCAIKit.ScreenShareServiceRequest) {
         switch request {
         case .session:
               // Show the prompt only for user-initiated sessions. Otherwise, skip the prompt.
               if screenShareInitiatedFrom == .endUser {
                  showScreenSharePrompt(.startSession)
               }
         case .remoteControl:
               showScreenSharePrompt(.remoteControl)
         @unknown default:
               handleError(ScreenShareError.unknownRequest, message: "Received unknown screen share request: \(request)")
         }
      }
   }
   ```
5. Full device screen sharing (Optional)

   Full device screen sharing enables agents to view screens from applications outside of your own, including system settings and inter-application navigation. This requires adding a Broadcast Extension when integrating the iOS SDK.

   > **Important:** Full device screen sharing requires additional setup and is only available on physical devices (not iOS Simulator).

   ##### 1. Add a Broadcast Extension target

   1. Open your Xcode project
   2. Navigate to File > New > Target...
   3. Pick "Broadcast Upload Extension"
   4. Enter a name for the target
   5. Uncheck "Include UI Extension"
   6. Create the target, noting its bundle ID
   7. Change the target SDK of your Broadcast Extension to iOS 12.0 or higher

   ##### 2. Set up Keychain Sharing

   Your app and the app extension need to share secrets via the iOS Keychain using their own Keychain group.

   In both your app target and your extension target, add a Keychain Sharing entitlement for the `io.cobrowse` keychain group.

   ##### 3. Add the bundle ID to your plist

   Add the extension bundle ID to your app's `Info.plist` (not the extension's `Info.plist`):

   ```xml
   <key>CBIOBroadcastExtension</key>
   <string>your.app.extension.bundle.ID.here</string>
   ```

   ##### 4. Add CobrowseSDK to your Broadcast Extension target

   Add the `CobrowseSDK` framework to your extension target:
   
   With your broadcast extension target selected, click the `+` button under **Frameworks and Libraries** and add `CobrowseSDK` to your extension target.

   ##### 5. Implement the extension

   Xcode will have added `SampleHandler.swift` file as part of the target you created earlier. Replace the content of the files with the following:
   ```swift
   import CobrowseSDK

   class SampleHandler: CobrowseIOReplayKitExtension { }
   ```

   ##### 6. Build and run your app

   You're now ready to build and run your app. The full device capability is only available on physical devices, it will not work in the iOS Simulator.

   If you've set everything up properly, after clicking the blue circular icon you should see a screen to select your Broadcast Extension.

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