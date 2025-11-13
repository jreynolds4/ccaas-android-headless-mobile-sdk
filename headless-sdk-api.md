# CCAI Mobile Headless SDK API Documentation

## iOS

```swift
import CCAIKit
import CCAIChat
import CCAIScreenShare

let client = CCAI.shared
```

## Android

```kotlin
import com.ccaiplatform.ccaikit.CCAI
import com.ccaiplatform.ccaichat.initializeChat
import com.ccaiplatform.ccaiscreenshare.initializeScreenShare

val client = CCAI
```

## Properties

| name | description |
| :------- | :-------- |
| shared | _(readonly)_ The singleton instance of the CCAI framework (iOS only) |
| authService | _(readonly)_ The authentication service for user authentication |
| companyService | _(readonly)_ The company service for retrieving company configurations |
| queueMenuService | _(readonly)_ The queue menu service for menu and wait time operations |
| optionsService | _(readonly)_ The options service for SDK configuration management |
| languageService | _(readonly)_ The language service for language code management |
| pushNotificationService | _(readonly)_ The push notification service for handling notifications |
| endUserService | _(readonly)_ The end user service for user information management (iOS only) |
| chatService | Only has a value when chat service is initialized |
| screenShareService | The current screen share service, or null if not initialized |

**Android Object Properties:**
- `CCAI` is an object (singleton) in Kotlin, so you access properties directly
- All services are accessible as properties of the CCAI object
- No need to call `getInstance()` - just use `CCAI.propertyName`

## Initialization

### Basic Initialization

To initialize the CCAI SDK, you need to provide the `InitOptions` which includes the company key, host URL, delegate, and optional parameters.

**iOS:**
```swift
let options = InitOptions(
    key: "your_company_key",
    urlHost: "your_host_url",
    delegate: yourDelegate,
    languageCode: "en"
)

do {
    try CCAI.shared.initialize(options: options)
} catch {
    print("Initialization failed with error: \(error)")
}
```

**Android:**
```kotlin
val options = InitOptions(
    key = "your_company_key",
    urlHost = "your_host_url",
    ccaiListener = yourListener,
    languageCode = "en"
)

try {
    CCAI.initialize(context, options)
} catch (e: Exception) {
    Log.e("CCAI", "Initialization failed with error: ${e.message}")
}
```

### InitOptions Configuration

The `InitOptions` contains all necessary configuration parameters:

**iOS:**
```swift
public struct InitOptions {
    /// The company key used for authentication
    public let key: String
    
    /// The host URL for CCAI platform (e.g., "my-unique-tenant.uc1.ccaiplatform.com")
    public let urlHost: String
    
    /// The preferred language code for localization (defaults to "en")
    public var languageCode: String
    
    /// The delegate that receives SDK events and callbacks
    public weak var delegate: CCAIDelegate?
    
    /// Whether to cache the authentication token (defaults to true)
    public let cacheAuthToken: Bool
}
```

**Android:**
```kotlin
data class InitOptions(
    var key: String,
    var urlHost: String,
    var languageCode: String? = "en"
    var ccaiListener: CCAIListener? = null,
){
    var cacheAuthToken: Boolean = true,
}
```

### Initialize Chat Service

**iOS:**
```swift
import CCAIChat

let chatOptions = ChatOptions(
    delegate: chatDelegate,
    downloadTranscriptVisibility: DownloadTranscriptVisibility.showAll,
    greeting: "Hello! How can I help you?"
)
CCAI.shared.initializeChat(chatOptions)
```

**Android:**
```kotlin
import com.ccaiplatform.ccaichat.initializeChat
import com.ccaiplatform.ccaichat.model.ChatOptions
import com.ccaiplatform.ccaichat.model.enum.DownloadTranscriptVisibility

val chatOptions = ChatOptions(
    webFormInterface = WebFormManager(),
    downloadTranscriptVisibility = DownloadTranscriptVisibility.SHOW_ALL,
    greeting = "Hello! How can I help you?"
)
CCAI.initializeChat(context, chatOptions, languageCode)
```

### Initialize Screen Share Service

**iOS:**
```swift
import CCAIScreenShare

let screenShareOptions = ScreenShareOptions(
    key: "your_screen_share_key",
    domain: "your_domain.com"
)
CCAI.shared.initializeScreenShare(screenShareOptions)
```

**Android:**
```kotlin
import com.ccaiplatform.ccaiscreenshare.initializeScreenShare

val screenShareOptions = ScreenShareOptions(
    key = "your_screen_share_key",
    domain = "your_domain.com"
)
CCAI.initializeScreenShare(context, screenShareOptions)
```

## Authentication

### CCAIDelegate Protocol

Your app must implement the `CCAIDelegate` protocol to handle authentication, should use the company secret for generating the JWT:

**iOS:**
```swift
public protocol CCAIDelegate: AnyObject {
    /// Authenticates the end user and returns an auth token
    func ccaiShouldAuthenticate() async -> String?
}
```

**Android:**
```kotlin
interface CCAIListener {
    suspend fun ccaiShouldAuthenticate(): String?
}
```

### Example Implementation

**iOS:**
```swift
class AuthController: CCAIDelegate {
    func ccaiShouldAuthenticate() async -> String? {
        var jwt = JWT(claims: claims)

        // First, sign JWT with company secret
        guard let secret = self.companySecret, let key = secret.data(using: .utf8) else { return nil }
        let signer = JWTSigner.hs384(key: key)
        guard let encodedJWT = try? jwt.sign(using: signer) else { return nil }
        
        // Then authenticate JWT using authService to get an auth token
        return try? await CCAI.shared.authService?.authenticate(encodedJWT)
    }
}
```

**Android:**
```kotlin
class AuthController : CCAIListener {
    override suspend fun ccaiShouldAuthenticate(): String? {
        // First, sign JWT with company secret
        val jwt = Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS384, companySecret?.encodeToByteArray())
            .compact()
        
        // Then authenticate JWT using authService to get an auth token
        return try {
            CCAI.authService?.authenticate(jwt)
        } catch (e: Exception) {
            null
        }
    }
}
```

## Company

| name | description |
| :------- | :-------- |
| get | Retrieves configured company information |

### `getCompany`

**Method Signature:**

**iOS:**
```swift
func get() async throws -> CompanyResponse
```

**Android:**
```kotlin
suspend fun get(): CompanyResponse
```

**Return Value:**
Returns a `CompanyResponse` object containing company configuration and settings.

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Interfaces:**
```swift
public struct CompanyResponse: Decodable {
    public let displayName: String?
    public let supportEmail: String?
    public let actionOnlyCallSettings: EnabledSetting?
    public let languages: [LanguageSetting]?
    public let phoneNumber: String?
    public let phoneNumberThreshold: Float
    public let faqSettings: FAQSetting?
    public let afterHoursSettings: AfterHoursSetting?
    public let chatSettings: ChatSetting?
    public let preventDirectPstnCall: Bool?
    public let emailEnhancementEnabled: Bool?
    public let pstnFallbackEnabled: Bool?
    public let actionTrackingEnabled: Bool?
}
```

**Example Usage:**

**iOS:**
```swift
do {
    let company = try await CCAI.shared.companyService?.get()
    print("Company: \(company.displayName ?? "Unknown")")
} catch {
    print("Failed to get company info: \(error)")
}
```

**Android:**
```kotlin
try {
    val company = CCAI.companyService?.get()
    Log.d("CCAI", "Company: ${company?.displayName ?: "Unknown"}")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to get company info: ${e.message}")
}
```

## Menus
| name | description |
| :------- | :-------- |
| get | List all available menus for given tenant |
| getWaitTimes | Get current wait times for various channels on a menu |

### `getMenus`

This method is used to list all the menu items available for the tenant.

**Method Signature:**

**iOS:**
```swift
func get(key: String?, language: String?) async throws -> ([QueueMenu], Bool)
```

**Android:**
```kotlin
suspend fun get(key: String?): MenuResult
```

**Return Value:**
Returns a tuple containing an array of `QueueMenu` objects and a boolean indicating if it's direct access.

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Interfaces:**
```swift
public struct QueueMenu: Decodable, Identifiable, Hashable {
    public let id: Int
    public let name: String?
    public let hidden: Bool?
    public let position: Int?
    public let settings: [QueueMenuSetting]?
    public let children: [QueueMenu]?
    public let channels: Channels?
    public let redirectionExtra: QueueMenuRedirection?
    public let deflectionFrom: QueueMenuDeflection?
}

public struct Channels: Decodable {
    public let email: EmailChannel?
    public let chat: ChatChannel?
    public let voiceCall: VoiceCallChannel?

    // it will be supported in a future release.
    public let externalDeflectionLink: ExternalDeflectionLink? 
}
```

**Example Usage:**

**iOS:**
```swift
do {
    let (menus, isDirectAccess) = try await CCAI.shared.queueMenuService?.get(key: "direct_menu_key", language: "en")
} catch {
    // handle error
}
```

**Android:**
```kotlin
try {
    val result = CCAI.queueMenuService?.get(directAccessKey)
    val menus = result?.menus.orEmpty()
    val isDirectAccess = result?.isDirectAccess
} catch (e: Exception) {
    // handle error
}
```

### `getWaitTimes`

This method is used to get the waiting time for chat channel and call channel of a menu.

**Method Signature:**

**iOS:**
```swift
func getWaitTimes(menuId: Int, language: String?) async throws -> QueueMenuWaitTimeResponse
```

**Android:**
```kotlin
suspend fun getWaitTimes(menuId: Int): QueueMenuWaitTimeResponse
```

**Return Value:**
Returns a `QueueMenuWaitTimeResponse` object.

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Interfaces:**
```swift
public struct QueueMenuWaitTimeResponse: Decodable {
    public let chat: Int?
    public let voiceCall: Int?
}
```

**Example Usage:**

**iOS:**
```swift
do {
    let waitTimes = try await CCAI.shared.queueMenuService?.getWaitTimes(menuId: 123, language: "en")
} catch {
    // handle error
}
```

**Android:**
```kotlin
try {
    val waitTimes = CCAI.queueMenuService?.getWaitTimes(123)
} catch (e: Exception) {
    // handle error
}
```

## Authentication Services

### `authenticate`

**Method Signature:**

**iOS:**
```swift
func authenticate(_ jwt: String) async throws -> String?
```

**Android:**
```kotlin
suspend fun authenticate(jwt: String): String?
```

**Return Value:**
Returns an authentication token string if authentication is successful, nil otherwise.

**Error Handling:**
Throws authentication or network related errors.

**Example Usage:**

**iOS:**
```swift
// Should use the company secret for generating the jwtToken
do {
    let authToken = try await CCAI.shared.authService?.authenticate(jwtToken)
    if let token = authToken {
        print("Authentication successful")
    }
} catch {
    print("Authentication failed: \(error)")
}
```

**Android:**
```kotlin
try {
    val authToken = CCAI.authService?.authenticate(jwtToken)
    if (authToken != null) {
        Log.d("CCAI", "Authentication successful")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Authentication failed: ${e.message}")
}
```

### `updateAuthToken`

**Method Signature:**

**iOS:**
```swift
func updateAuthToken(_ token: String?)
```

**Android:**
```kotlin
fun updateAuthToken(token: String?)
```

**Parameters:**
- `token`: The new authentication token to use, or nil to clear the current token

**Example Usage:**

**iOS:**
```swift
// Set new token
CCAI.shared.authService?.updateAuthToken("new_auth_token")

// Clear token
CCAI.shared.authService?.updateAuthToken(nil)
```

**Android:**
```kotlin
// Set new token
CCAI.authService?.updateAuthToken("new_auth_token")

// Clear token
CCAI.authService?.updateAuthToken(null)
```

## Language Services

### `getSdkAvailableLanguageCodes`

**Method Signature:**

**iOS:**
```swift
func getSdkAvailableLanguageCodes() -> [String]
```

**Android:**
```kotlin
fun getSdkAvailableLanguageCodes(): List<String>
```

**Return Value:**
Returns an array of language codes available in the SDK.

**Example Usage:**

**iOS:**
```swift
let sdkLanguages = CCAI.shared.languageService?.getSdkAvailableLanguageCodes()
print("SDK languages: \(sdkLanguages)")
```

**Android:**
```kotlin
val sdkLanguages = CCAI.languageService?.getSdkAvailableLanguageCodes()
Log.d("CCAI", "SDK languages: $sdkLanguages")
```

### `setLanguageCode`

**Method Signature:**

**iOS:**
```swift
func setLanguageCode(_ code: String)
```

**Android:**
```kotlin
fun setLanguageCode(code: String)
```

**Parameters:**
- `code`: The language code to set (e.g., "en", "es")

**Example Usage:**

**iOS:**
```swift
CCAI.shared.languageService?.setLanguageCode("es")
```

**Android:**
```kotlin
CCAI.languageService?.setLanguageCode("es")
```

## End User Services (iOS only)

### `endUser`

**Property:**
```swift
var endUser: EndUser? { get }
```

**Return Value:**
Returns the current end user information.

**Interfaces:**
```swift
public struct EndUser: Codable {
    public let id: Int?
    public let name: String?
    public let identifier: String?
    public let email: String?
    public let phone: String?
    
    public var displayName: String { name ?? email ?? identifier ?? "Customer" }
}
```

**Example Usage:**
```swift
if let user = CCAI.shared.endUserService?.endUser {
    print("Current user: \(user.displayName)")
}
```

### `updateEndUser`

**Method Signature:**
```swift
func updateEndUser(_ endUser: EndUser?)
```

**Parameters:**
- `endUser`: The new end user information to set, or nil to clear

**Example Usage:**
```swift
let newUser = EndUser(
    id: 123,
    name: "John Doe",
    identifier: "user123",
    email: "john@example.com",
    phone: "+1234567890"
)
CCAI.shared.endUserService?.updateEndUser(newUser)
```

## Push Notifications

### `registerForPushNotifications` (iOS only)

**Method Signature:**
```swift
func registerForPushNotifications(options: UNAuthorizationOptions, completion: ((Bool) -> Void)?) async throws
```

**Parameters:**
- `options`: The notification options (alert, badge, sound)
- `completion`: Optional completion handler called with grant status

**Example Usage:**
```swift
let options: UNAuthorizationOptions = [.alert, .sound]
try await CCAI.shared.pushNotificationService?.registerForPushNotifications(options: options) { granted in
    if granted {
        print("Push notifications granted")
    } else {
        print("Push notifications denied")
    }
}
```

**Android:**
Android uses a different approach for push notification permissions. Use the following utility methods:

```kotlin
// Request notification permissions
PermissionUtil.requestPermissionsForNotifications(activity)

// Check if permissions are granted
val isGranted = PermissionUtil.isPermissionsForNotificationsGranted(context)
if (isGranted) {
    Log.d("CCAI", "Push notifications granted")
} else {
    Log.d("CCAI", "Push notifications denied")
}
```

### `updatePushToken`

**Method Signature:**

**iOS:**
```swift
func updatePushToken(data: Data?, type: PushType)
```

**Android:**
```kotlin
fun updatePushToken(token: String)
```

**Parameters:**
- `token`: The push token string

**Example Usage:**

**iOS:**
```swift
let pushTokenData: Data = ... // Your push token data
CCAI.shared.pushNotificationService?.updatePushToken(data: pushTokenData, type: .apns)
```

**Android:**
```kotlin
val pushToken: String = "your_firebase_token" // Your push token
CCAI.pushNotificationService?.updatePushToken(pushToken)
```

## Chats

| name | description |
| :------- | :-------- |
| start | Starts a new chat session |
| resumeChat | Resumes a previously established chat session |
| enqueueCurrentChat | Enqueues the current chat session for later retrieval |
| getLastChatInProgress | Retrieves the last chat session that was in progress |
| getPreviousChats | Retrieves the previous chats for a given page |
| sendMessage | Sends a message in the current chat session |
| sendMessagePreview | Sends a message preview while the user is typing |
| typing | Updates the typing status in the current chat session |
| endChat | Ends the current chat session gracefully |
| escalateToHumanAgent | Escalates the current chat session to a human agent |
| checkStatus | Refresh the current chat and checks the current status |
| fetchWebForm | Fetches a web form request in the chat session |
| validateWebForm | Validates a web form response returned by the host application |
| escalate | Escalates the current chat session with options |
| sendEvent | Sends a chat event to the current chat session |
| getCustomFormDetails | Retrieves the details of a custom form |
| submitCustomForm | Submits a custom form with the provided request data |
| notifySmartActionReceived | Notifies the chat service that a smart action has been received |
| updateSmartActionStatus | Updates the status of a smart action |
| startPostSession | Updates the post-session transfer status to be in progress |
| readyPostSession | Updates the post-session transfer status to be ready |
| downloadChatTranscript | Downloads the chat transcript to a cache file |
| downloadChatTranscriptData | Downloads the chat transcript and returns the PDF data directly (iOS only) |

### `start`

**Method Signature:**

**iOS:**
```swift
func start(request: ChatRequest) async throws
```

**Android:**
```kotlin
suspend fun start(request: ChatRequest)
```

**Parameters:**
- `request`: The chat request containing configuration for the new chat

**Return Value:**
No return value. This method starts a new chat session with the provided chat request.

**What it does:**
- Starts a new chat session with the provided chat request
- This function will create a new chat and connect to the chat provider

**Error Handling:**
Throws on error; use `try/catch` in your implementation.

**Interfaces:**
```swift
public struct ChatRequest: Encodable {
    public struct Chat: Encodable {
        let menuId: Int
        let lang: String
        let ticketId: String?
        let greeting: String?
        let customData: CustomData?
        let displayOrderInAdapter: [String]?
    }
    
    public let chat: Chat
    public let isScreenShareable: Bool?
}

public struct ChatResponse: Decodable {
    public let id: Int
    public let lang: String?
    public let status: ChatStatus?
    public let statusText: String?
    public let providerType: String?
    public let providerChannelId: String?
    public let region: String?
    public let timeoutAt: String?
    public let agent: HumanAgent?
    public let virtualAgent: VirtualAgent?
    public let allAgents: [HumanAgent]?
    public let allVirtualAgents: [VirtualAgent]?
    public let menus: [QueueMenu]?
    public let features: [String]?
    public let postSessionRequired: Bool?
    public let postSessionTransferStatus: PostSessionTransferStatus?
    public let postSessionOptInRequired: Bool?
}
```

**Example Usage:**

**iOS:**
```swift
let chatRequest = ChatRequest(menuId: 123, lang: "en")
do {
    try await CCAI.shared.chatService?.start(request: chatRequest)
    // Chat started successfully
} catch {
    print("Failed to start chat: \(error)")
}
```

**Android:**
```kotlin
val chatRequest = ChatRequest(
    chat = Chat(
        menuId = 123,
        languageCode = "en"
    )
)
try {
    CCAI.chatService?.start(chatRequest)
    // Chat started successfully
} catch (e: Exception) {
    Log.e("CCAI", "Failed to start chat: ${e.message}")
}
```

### `resumeChat`

**Method Signature:**

**iOS:**
```swift
func resumeChat(_ chat: ChatResponse) async throws
```

**Android:**
```kotlin
suspend fun resumeChat(chat: ChatResponse)
```

**Return Value:**
No return value. This method resumes an existing chat session.

**What it does:**
- Resumes an existing chat session

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.chatService?.resumeChat(chat)
} catch {
    // handle error
}
```

**Android:**
```kotlin
try {
    CCAI.chatService?.resumeChat(chat)
} catch (e: Exception) {
    // handle error
}
```

### `enqueueCurrentChat`

**Method Signature:**

**iOS:**
```swift
func enqueueCurrentChat() async throws
```

**Android:**
```kotlin
suspend fun enqueueCurrentChat()
```

**Return Value:**
No return value. 

**What it does:**
- Enqueues the current chat session for later retrieval
- Automatically calls checkStatus() to verify the chat status after enqueuing

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.chatService?.enqueueCurrentChat()
    print("Chat enqueued successfully")
} catch {
    print("Failed to enqueue chat: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.chatService?.enqueueCurrentChat()
    Log.d("CCAI", "Chat enqueued successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to enqueue chat: ${e.message}")
}
```

### `getLastChatInProgress`

**Method Signature:**

**iOS:**
```swift
func getLastChatInProgress() async throws -> ChatResponse?
```

**Android:**
```kotlin
suspend fun getLastChatInProgress(): ChatResponse?
```

**Return Value:**
Returns a `ChatResponse` instance if found, or `nil` if no ongoing chat exists.

**What it does:**
- Retrieves the last chat that is still in progress

**Error Handling:**
Throws on error during loading; use `try/catch` in your implementation.

**Example Usage:**

**iOS:**
```swift
do {
    let chat = try await CCAI.shared.chatService?.getLastChatInProgress()
    if let chat = chat {
        print("Found ongoing chat: \(chat.id)")
    } else {
        print("No ongoing chat found")
    }
} catch {
    print("Failed to get last chat: \(error)")
}
```

**Android:**
```kotlin
try {
    val chat = CCAI.chatService?.getLastChatInProgress()
    if (chat != null) {
        Log.d("CCAI", "Found ongoing chat: ${chat.id}")
    } else {
        Log.d("CCAI", "No ongoing chat found")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Failed to get last chat: ${e.message}")
}
```

### `getPreviousChats`

**Method Signature:**

**iOS:**
```swift
func getPreviousChats(currentPage: Int) async throws -> PreviousChat?
```

**Android:**
```kotlin
suspend fun getPreviousChats(currentPage: Int): PreviousChat?
```

**Parameters:**
- `currentPage`: The page number for pagination

**Return Value:**
Returns a `PreviousChat` object containing the previous chats for the specified page.

**What it does:**
- Retrieves previous chat history with pagination

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    let previousChats = try await CCAI.shared.chatService?.getPreviousChats(currentPage: 1)
    if let chats = previousChats {
        print("Found \(chats.messages.count) previous messages")
    }
} catch {
    print("Failed to get previous chats: \(error)")
}
```

**Android:**
```kotlin
try {
    val previousChats = CCAI.chatService?.getPreviousChats(1)
    if (previousChats != null) {
        Log.d("CCAI", "Found ${previousChats.messages.size} previous messages")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Failed to get previous chats: ${e.message}")
}
```

### `sendMessage`

**Method Signature:**

**iOS:**
```swift
func sendMessage(_ message: OutgoingMessageContent) async throws
```

**Android:**
```kotlin
suspend fun sendMessage(message: OutgoingMessageContent)
```

**Parameters:**
- `message`: The outgoing message content to send

**Return Value:**
No return value. This method sends a message in the current chat session.

**What it does:**
- Sends a message in the current chat session
- If Task VA is active, sends the message through Task VA; otherwise sends through the regular provider
- Supports multiple message types including text, images, videos, form completion events, and screen share events
- Use `formComplete` message type to notify the server when a form has been completed by the user
- Use `screenShare` message type to send screen sharing events and responses to the server
- Message routing depends on the current chat state and active virtual agent configuration

**Error Handling:**
Throws on error; use `try/catch` in your implementation.

**Message Types:**
```swift
public enum OutgoingMessageContent {
    case text(content: String)
    case images(images: [Data], smartAction: SmartAction?, contentType: String)
    case videos(videoUrls: [String], smartAction: SmartAction?, contentType: String)
    case formComplete(payload: FormCompletePayload)
    case screenShare(event: ChatMessageEvent, response: ScreenShareResponse?)
}
```

**Screen Share Message Type:**

The `screenShare` message type is used to send screen sharing events and responses to the server. This is particularly useful for coordinating screen sharing sessions between the end user and agent.

**Available Screen Share Events:**
- `screenShareRequestedFromAgent` - Agent requested screen sharing
- `screenShareRequestedFromEndUser` - End user requested screen sharing  
- `screenShareCodeGenerated` - Screen share code was generated
- `screenShareFailed` - Screen sharing failed
- `screenShareStarted` - Screen sharing session started
- `screenShareEnded` - Screen sharing session ended


**Example Usage:**

**iOS:**
```swift
// Send text message
do {
    try await CCAI.shared.chatService?.sendMessage(.text(content: "Hello world"))
} catch {
    print("Failed to send message: \(error)")
}

// Send image message
let imageData = UIImage(named: "photo")?.jpegData(compressionQuality: 0.8) ?? Data()
do {
    try await CCAI.shared.chatService?.sendMessage(.images(images: [imageData], smartAction: nil, contentType: "image/jpeg"))
} catch {
    print("Failed to send image: \(error)")
}

// Send form completion event
let formCompletePayload = FormCompletePayload(
    type: "form_complete",
    signature: "form_signature",
    data: FormCompleteData(
        smartActionId: 123,
        status: .success,
        timestamp: "2024-01-01T12:00:00Z",
        details: FormCompleteDataDetails(message: "Form submitted successfully")
    )
)
do {
    try await CCAI.shared.chatService?.sendMessage(.formComplete(payload: formCompletePayload))
    print("Form completion event sent")
} catch {
    print("Failed to send form completion: \(error)")
}
```

**Android:**
```kotlin
// Send text message
try {
    CCAI.chatService?.sendMessage(OutgoingMessageContent.text("Hello world"))
} catch (e: Exception) {
    Log.e("CCAI", "Failed to send message: ${e.message}")
}

// Send image message
val imageData = getImageData() // Your image data
try {
    CCAI.chatService?.sendMessage(OutgoingMessageContent.images(listOf(imageData), null, "image/jpeg"))
} catch (e: Exception) {
    Log.e("CCAI", "Failed to send image: ${e.message}")
}

// Send form completion event
val formCompletePayload = FormCompletePayload(
    type = "form_complete",
    signature = "form_signature",
    data = FormCompleteData(
        smartActionId = 123,
        status = FormCompleteStatus.SUCCESS,
        timestamp = "2024-01-01T12:00:00Z",
        details = FormCompleteDataDetails(message = "Form submitted successfully")
    )
)
try {
    CCAI.chatService?.sendMessage(OutgoingMessageContent.formComplete(formCompletePayload))
    Log.d("CCAI", "Form completion event sent")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to send form completion: ${e.message}")
}
```

### `sendMessagePreview`

**Method Signature:**

**iOS:**
```swift
func sendMessagePreview(_ messagePreview: String) async throws
```

**Android:**
```kotlin
suspend fun sendMessagePreview(messagePreview: String)
```

**Parameters:**
- `messagePreview`: The preview text to send

**Return Value:**
No return value. This method sends a message preview to the web while the user is typing.

**What it does:**
- Sends a message preview to the web while the user is typing
- Only works when Task VA is not active

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.chatService?.sendMessagePreview("Typing...")
} catch {
    print("Failed to send message preview: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.chatService?.sendMessagePreview("Typing...")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to send message preview: ${e.message}")
}
```

### `typing`

**Method Signature:**

**iOS:**
```swift
func typing(_ isTyping: Bool)
```

**Android:**
```kotlin
fun typing(isTyping: Boolean)
```

**Parameters:**
- `isTyping`: true if the user is typing, false otherwise

**Return Value:**
No return value. This method updates the typing status in the current chat session.

**What it does:**
- Updates the typing status in the current chat session

**Example Usage:**

**iOS:**
```swift
// Start typing
CCAI.shared.chatService?.typing(true)

// Stop typing
CCAI.shared.chatService?.typing(false)
```

**Android:**
```kotlin
// Start typing
CCAI.chatService?.typing(true)

// Stop typing
CCAI.chatService?.typing(false)
```

### `endChat`

**Method Signature:**

**iOS:**
```swift
func endChat() async throws
```

**Android:**
```kotlin
suspend fun endChat()
```

**Return Value:**
No return value. This method ends the current chat session.

**What it does:**
- Ends the current chat session
- Disconnects from the chat provider and marks the chat as finished on the server

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.chatService?.endChat()
    print("Chat ended successfully")
} catch {
    print("Failed to end chat: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.chatService?.endChat()
    Log.d("CCAI", "Chat ended successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to end chat: ${e.message}")
}
```

### `escalateToHumanAgent`

**Method Signature:**

**iOS:**
```swift
func escalateToHumanAgent() async throws
```

**Android:**
```kotlin
suspend fun escalateToHumanAgent()
```

**Return Value:**
No return value. This method escalates the current chat to a human agent.

**What it does:**
- Escalates the current chat to a human agent
- Transfers the chat from virtual agent to a human agent and checks the updated status

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.chatService?.escalateToHumanAgent()
    print("Chat escalated to human agent")
} catch {
    print("Failed to escalate chat: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.chatService?.escalateToHumanAgent()
    Log.d("CCAI", "Chat escalated to human agent")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to escalate chat: ${e.message}")
}
```

### `checkStatus`

**Method Signature:**

**iOS:**
```swift
func checkStatus() async throws
```

**Android:**
```kotlin
suspend fun checkStatus()
```

**Return Value:**
No return value.

**What it does:**
- Retrieves the latest chat information from the server and updates internal state

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.chatService?.checkStatus()
    print("Status check completed successfully")
} catch {
    print("Failed to check status: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.chatService?.checkStatus()
    Log.d("CCAI", "Status check completed successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to check status: ${e.message}")
}
```

### `fetchWebForm`

**Method Signature:**

**iOS:**
```swift
func fetchWebForm(_ webForm: WebFormRequest) async throws -> WebFormResponse?
```

**Android:**
```kotlin
suspend fun fetchWebForm(externalFormId: String, smartActionId: Int): WebFormResponse
```

**Return Value:**
Returns a `WebFormResponse` object containing the web form data.

**Data Structure:**
```swift
// iOS
public typealias WebFormResponse = FormPayload<WebFormData>

public struct FormPayload<T: Codable>: Codable {
    public let type: String              // Form type identifier
    public let signature: String?        // Form signature for validation
    public let data: T                   // Form data payload
}

public struct WebFormData: Codable {
    public let smartActionId: Int        // Smart action identifier
    public let externalFormId: String    // External form identifier
    public let uri: String               // Form URI/URL
}
```

```kotlin
// Android
data class WebFormResponse(
    val type: String? = null,           // Form type identifier
    val signature: String? = null,      // Form signature for validation
    val data: WebFormData? = null       // Form data payload
)

data class WebFormData(
    val externalFormId: String? = null, // External form identifier
    val smartActionId: Int = 0,         // Smart action identifier
    val uri: String? = null             // Form URI/URL
)
```

**What it does:**
- Gets the web form data

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
let webFormRequest = WebFormRequest(
    signature: "signature",
    smartActionId: 123,
    externalFormId: "form123"
)

do {
    let webFormResponse = try await CCAI.shared.chatService?.fetchWebForm(webFormRequest)
    if let response = webFormResponse {
        print("Web form received: \(response)")
    } else {
        print("No web form available")
    }
} catch {
    print("Failed to fetch web form: \(error)")
}
```

**Android:**
```kotlin
try {
    val webFormResponse = CCAI.chatService?.fetchWebForm("form123", 123)
    Log.d("CCAI", "Web form received: $webFormResponse")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to fetch web form: ${e.message}")
}
```

### `validateWebForm`

**Method Signature:**

**iOS:**
```swift
func validateWebForm(_ webForm: WebFormResponse) async throws -> Bool
```

**Android:**
```kotlin
suspend fun validateWebForm(formData: WebFormResponse): Boolean
```

**Return Value:**
Returns `true` if the web form is valid, `false` otherwise.

**What it does:**
- Validates the provided form data with the server

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    let isValid = try await CCAI.shared.chatService?.validateWebForm(webFormResponse)
    if isValid == true {
        print("Web form is valid")
    } else {
        print("Web form validation failed")
    }
} catch {
    print("Failed to validate web form: \(error)")
}
```

**Android:**
```kotlin
try {
    val isValid = CCAI.chatService?.validateWebForm(webFormResponse)
    if (isValid == true) {
        Log.d("CCAI", "Web form is valid")
    } else {
        Log.d("CCAI", "Web form validation failed")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Failed to validate web form: ${e.message}")
}
```

### `escalate`

**Method Signature:**

**iOS:**
```swift
func escalate(option: ChatEscalationOption) async throws
```

**Android:**
```kotlin
suspend fun escalate(option: ChatEscalationOption)
```

**Return Value:**
No return value.

**What it does:**
- Escalates the current chat with deflection using escalation option
- Sends escalation request to the server with escalation ID and deflection channel

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
let escalationOption = ChatEscalationOption(
    escalationId: 123,
    deflectionChannel: "email"  // Note: Email channel support coming in future release
)

do {
    try await CCAI.shared.chatService?.escalate(option: escalationOption)
    print("Chat escalated successfully")
} catch {
    print("Failed to escalate chat: \(error)")
}
```

**Android:**
```kotlin
val escalationOption = ChatEscalationOption(
    escalationId = 123,
    deflectionChannel = "email"  // Note: Email channel support coming in future release
)
try {
    CCAI.chatService?.escalate(escalationOption)
    Log.d("CCAI", "Chat escalated successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to escalate chat: ${e.message}")
}
```

### `sendEvent`

**Method Signature:**

**iOS:**
```swift
func sendEvent(_ event: ChatEvent) async throws
```

**Android:**
```kotlin
suspend fun sendEvent(event: ChatEvent)
```

**Return Value:**
No return value. 

**What it does:**
- Sends a chat event to the server for tracking user interactions
- Common use cases include:
  - Form interactions (e.g., "form_received", "form_clicked")
  - Smart action events (e.g., "smart_action_received", "smart_action_clicked")
  - Custom user interactions for analytics and tracking
- Events are used for monitoring user behavior and improving chat experience

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
let event = ChatEvent(
    name: "form_received",
    payload: ["name": "contact_form"]
)

do {
    try await CCAI.shared.chatService?.sendEvent(event)
    print("Event sent successfully")
} catch {
    print("Failed to send event: \(error)")
}
```

**Android:**
```kotlin
val event = ChatEvent(
    name = "form_received",
    payload = mapOf("name" to "contact_form")
)

try {
    CCAI.chatService?.sendEvent(event)
    Log.d("CCAI", "Event sent successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to send event: ${e.message}")
}
```

### `getCustomFormDetails`

**Method Signature:**

**iOS:**
```swift
func getCustomFormDetails(formId: Int) async throws -> CustomFormDetailsResponse?
```

**Android:**
```kotlin
suspend fun getCustomFormDetails(formId: Int): CustomFormDetailsResponse?
```

**Return Value:**
Returns a `CustomFormDetailsResponse` object containing the custom form details, or `null` if the form is not found.

**Data Structure:**
```swift
public struct CustomFormDetailsResponse: Decodable {
    public let title: String?           // Form title
    public let header: String?          // Form header text
    public let footer: String?          // Form footer text
    public let questions: [CustomFormQuestion]  // Array of form questions
}

public struct CustomFormQuestion: Decodable {
    public let id: Int                  // Question unique identifier
    public let type: CustomFormQuestionType  // Question type (text_entry, list_picker, date, time, toggle)
    public let isMandatory: Bool        // Whether the question is required
    public let isMasked: Bool          // Whether input should be masked (e.g., password)
    public let position: Int           // Question position in the form
    public let question: String        // Question text
    public let placeholder: String?    // Input placeholder text
    public let contentType: String?    // Content type for validation
    public let characterLimit: Int?    // Maximum character limit
    public let helpText: String?       // Help text for the question
    public let isMultiselect: Bool?    // Whether multiple selections are allowed
    public let toggleOnText: String?   // Text for toggle when ON
    public let toggleOffText: String?  // Text for toggle when OFF
    public let options: [CustomFormQuestionOption]?  // Available options for list picker
}

public struct CustomFormQuestionOption: Decodable {
    public let id: Int                 // Option unique identifier
    public let value: String?          // Option display value
    public let image: String?          // Option image URL
    public let listPosition: Int       // Position in the options list
}

public enum CustomFormQuestionType: String, Decodable {
    case textEntry = "text_entry"      // Text input field
    case listPicker = "list_picker"    // Dropdown/selection list
    case date = "date"                 // Date picker
    case time = "time"                 // Time picker
    case toggle = "toggle"             // Boolean toggle/switch
}
```

**What it does:**
- Retrieves the details of a custom form based on the provided form ID
- Returns a [CustomFormDetailsResponse] containing the details of the custom form, or `null` if the form is not found

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    let formDetails = try await CCAI.shared.chatService?.getCustomFormDetails(formId: 123)
    if let details = formDetails {
        print("Form Title: \(details.title ?? "No title")")
        print("Questions: \(details.questions?.count ?? 0)")
    } else {
        print("Form not found")
    }
} catch {
    print("Failed to get custom form details: \(error)")
}
```

**Android:**
```kotlin
try {
    val formDetails = CCAI.chatService?.getCustomFormDetails(123)
    if (formDetails != null) {
        Log.d("CCAI", "Form Title: ${formDetails.title ?: "No title"}")
        Log.d("CCAI", "Questions: ${formDetails.questions?.size ?: 0}")
    } else {
        Log.d("CCAI", "Form not found")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Failed to get custom form details: ${e.message}")
}
```

### `submitCustomForm`

**Method Signature:**

**iOS:**
```swift
func submitCustomForm(request: SubmitCustomFormRequest) async throws
```

**Android:**
```kotlin
suspend fun submitCustomForm(request: SubmitCustomFormRequest)
```

**Return Value:**
No return value.

**What it does:**
- Submits a custom form with the provided request data

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
let request = SubmitCustomFormRequest(
    smartActionId: 123,
    formResponse: [
        SubmitCustomFormRequest.Answer(questionId: 1, value: "John Doe"),
        SubmitCustomFormRequest.Answer(questionId: 2, value: "john@example.com")
    ]
)

do {
    try await CCAI.shared.chatService?.submitCustomForm(request: request)
    print("Form submitted successfully")
} catch {
    print("Failed to submit form: \(error)")
}
```

**Android:**
```kotlin
val request = SubmitCustomFormRequest(
    smartActionId = 123,
    formResponse = listOf(
        SubmitCustomFormRequest.Answer(questionId = 1, value = "John Doe"),
        SubmitCustomFormRequest.Answer(questionId = 2, value = "john@example.com")
    )
)

try {
    CCAI.chatService?.submitCustomForm(request)
    Log.d("CCAI", "Form submitted successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to submit form: ${e.message}")
}
```

### `notifySmartActionReceived`

**Method Signature:**

**iOS:**
```swift
func notifySmartActionReceived(commId: Int, commType: String, smartActionId: Int) async throws
```

**Android:**
```kotlin
suspend fun notifySmartActionReceived(commId: Int, commType: String, smartActionId: Int)
```

**Return Value:**
No return value.

**What it does:**
- Notifies the server that a smart action has been received
- Used for tracking smart action interactions in chat sessions

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.chatService?.notifySmartActionReceived(
        commId: 123,
        commType: "chats",
        smartActionId: 456
    )
    print("Smart action notification sent successfully")
} catch {
    print("Failed to notify smart action: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.chatService?.notifySmartActionReceived(
        commId = 123,
        commType = "chats",
        smartActionId = 456
    )
    Log.d("CCAI", "Smart action notification sent successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to notify smart action: ${e.message}")
}
```

### `updateSmartActionStatus`

**Method Signature:**

**iOS:**
```swift
func updateSmartActionStatus(commId: Int, commType: String, smartActionId: Int, status: SmartActionStatus) async throws
```

**Android:**
```kotlin
suspend fun updateSmartActionStatus(commId: Int, commType: String, smartActionId: Int, status: SmartActionStatus)
```

**Return Value:**
No return value.

**What it does:**
- Sends the current status of a smart action to the server for tracking

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
let status = SmartActionStatus.finished

do {
    try await CCAI.shared.chatService?.updateSmartActionStatus(
        commId: 123,
        commType: "chats",
        smartActionId: 456,
        status: status
    )
    print("Smart action status updated successfully")
} catch {
    print("Failed to update smart action status: \(error)")
}
```

**Android:**
```kotlin
val status = SmartActionStatus.Finished

try {
    CCAI.chatService?.updateSmartActionStatus(
        commId = 123,
        commType = "chats",
        smartActionId = 456,
        status = status
    )
    Log.d("CCAI", "Smart action status updated successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to update smart action status: ${e.message}")
}
```

### `startPostSession`

**Method Signature:**

**iOS:**
```swift
func startPostSession() async throws -> ChatResponse?
```

**Android:**
```kotlin
suspend fun startPostSession(): ChatResponse?
```

**Return Value:**
Returns a `ChatResponse` object containing the details of the post-session, or `null` if the process fails.

**What it does:**
- Starts the post-session process for the current chat
- Updates the post-session transfer status to "in_progress"

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    let postSessionResponse = try await CCAI.shared.chatService?.startPostSession()
    if let response = postSessionResponse {
        print("Post-session started successfully: \(response.id)")
    } else {
        print("Failed to start post-session")
    }
} catch {
    print("Failed to start post-session: \(error)")
}
```

**Android:**
```kotlin
try {
    val postSessionResponse = CCAI.chatService?.startPostSession()
    if (postSessionResponse != null) {
        Log.d("CCAI", "Post-session started successfully: ${postSessionResponse.id}")
    } else {
        Log.d("CCAI", "Failed to start post-session")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Failed to start post-session: ${e.message}")
}
```

### `readyPostSession`

**Method Signature:**

**iOS:**
```swift
func readyPostSession() async throws -> ChatResponse?
```

**Android:**
```kotlin
suspend fun readyPostSession(): ChatResponse?
```

**Return Value:**
Returns a `ChatResponse` object containing the details of the ready post-session, or `null` if preparation fails.

**What it does:**
- Prepares the post-session process for the current chat
- Updates the post-session transfer status to "ready"

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    let readyPostSessionResponse = try await CCAI.shared.chatService?.readyPostSession()
    if let response = readyPostSessionResponse {
        print("Post-session ready: \(response.id)")
    } else {
        print("Failed to prepare post-session")
    }
} catch {
    print("Failed to prepare post-session: \(error)")
}
```

**Android:**
```kotlin
try {
    val readyPostSessionResponse = CCAI.chatService?.readyPostSession()
    if (readyPostSessionResponse != null) {
        Log.d("CCAI", "Post-session ready: ${readyPostSessionResponse.id}")
    } else {
        Log.d("CCAI", "Failed to prepare post-session")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Failed to prepare post-session: ${e.message}")
}
```

### `downloadChatTranscript`

**Method Signature:**

**iOS:**
```swift
func downloadChatTranscript(to destination: URL) async throws
```

**Android:**
```kotlin
suspend fun downloadChatTranscript(): File?
```

**Return Value:**
Returns the downloaded PDF file.

**What it does:**
- Downloads the PDF transcript for the current chat session
- Generates a transcript ID and downloads the PDF file with retry logic

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
let transcriptURL = documentsPath.appendingPathComponent("chat_transcript.pdf")

do {
    try await CCAI.shared.chatService?.downloadChatTranscript(to: transcriptURL)
    print("Transcript downloaded to: \(transcriptURL.path)")
} catch {
    print("Failed to download transcript: \(error)")
}
```

**Android:**
```kotlin
try {
    val transcriptFile = CCAI.chatService?.downloadChatTranscript()
    if (transcriptFile != null) {
        Log.d("CCAI", "Transcript downloaded to: ${transcriptFile.absolutePath}")
    }
} catch (e: Exception) {
    Log.e("CCAI", "Failed to download transcript: ${e.message}")
}
```

### `downloadChatTranscriptData` (iOS only)

**Method Signature:**

```swift
func downloadChatTranscriptData() async throws -> Data
```

**Return Value:**
Returns the PDF data of the chat transcript.

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

```swift
do {
    let transcriptData = try await CCAI.shared.chatService?.downloadChatTranscriptData()
    if let data = transcriptData {
        // Use the PDF data directly
        print("Transcript data size: \(data.count) bytes")
    }
} catch {
    print("Failed to download transcript data: \(error)")
}
```

## Screen Share

| name | description |
| :------- | :-------- |
| updateOptions | Updates the screen share service options |
| create | Creates a new screen share session and returns a cobrowse code |
| activate | Activates the screen share session |
| end | Ends the current screen share session |
| clear | Clears the screen share state (iOS only) |
| enableRemoteControl | Enables or disables remote control functionality |
| enableFullDeviceSharing | Enables or disables full device sharing |
| sessionState | Gets the current session state (Android only) |
| isAvailable | Checks if screen sharing is available (Android only) |
    
### `updateOptions`

**Method Signature:**

**iOS:**
```swift
func updateOptions(_ options: ScreenShareOptions)
```

**Android:**
```kotlin
fun updateOptions(options: ScreenShareOptions)
```

**Parameters:**
- `options`: Updated screen share configuration options

**What it does:**
- Updates the screen share service configuration
- Reinitializes the provider with new options

**Error Handling:**
Throws on error; use `try/catch` in your implementation.

**Example Usage:**

**iOS:**
```swift
let newOptions = ScreenShareOptions(key: "new_key", domain: "https://new-domain.com")
do {
    try CCAI.shared.screenShareService?.updateOptions(newOptions)
    print("Screen share options updated successfully")
} catch {
    print("Failed to update screen share options: \(error)")
}
```

**Android:**
```kotlin
val newOptions = ScreenShareOptions(
    key = "new_key",
    domain = "https://new-domain.com"
)
try {
    CCAI.screenShareService?.updateOptions(newOptions)
    Log.d("CCAI", "Screen share options updated successfully")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to update screen share options: ${e.message}")
}
```

### `create`

**Method Signature:**

**iOS:**
```swift
func create(request: ScreenShareRequest) async throws -> ScreenShareResponse
```

**Android:**
```kotlin
suspend fun create(request: ScreenShareRequest, callbacks: ScreenShareCallbacks)
```

**Parameters:**
- `request`: The screen share request containing communication details
- `callbacks` (Android only): Optional callback functions for session events

**Return Value:**
- **iOS**: Returns a `ScreenShareResponse` object containing the cobrowse code and device ID
- **Android**: No return value. The response is provided through the `onSessionDidSucceed` callback.

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Android Callbacks:**
```kotlin
data class ScreenShareCallbacks(
    /** Called when session state changes */
    val onSessionStateChanged: ((ScreenShareSessionState) -> Unit)? = null,
    /** Called when activation permission is requested */
    val onSessionActivationRequest: (() -> Unit)? = null,
    /** Called when remote control permission is requested */
    val onSessionRemoteControlRequest: (() -> Unit)? = null,
    /** Called when full device access is requested */
    val onSessionFullDeviceRequest: (() -> Unit)? = null,
    /** Called when session creation fails */
    val onSessionCreationError: ((Error) -> Unit)? = null,
    /** Called when screen share session is successfully created */
    val onSessionDidSucceed: ((ScreenShareResponse) -> Unit)? = null
)
```

**Interfaces:**
```swift
public struct ScreenShareRequest {
    public let communicationId: Int
    public let communicationType: CommunicationType
    public let initiatedFrom: ScreenShareFrom
}

public enum CommunicationType: String {
    case chat
    case call
}

public enum ScreenShareFrom: String, Encodable, CaseIterable {
    case agent
    case endUser = "end_user"
}

public struct ScreenShareResponse: Encodable {
    public let code: ScreenShareCode
    public let deviceId: String
}

public struct ScreenShareCode: Encodable {
    public let type: String
    public let data: String
}
```

**Example Usage:**

**iOS:**
```swift
let request = ScreenShareRequest(
    communicationId: 123,
    communicationType: .chat,
    initiatedFrom: .endUser
)

do {
    let response = try await CCAI.shared.screenShareService?.create(request: request)
    print("Cobrowse code: \(response.code.data)")
} catch {
    print("Failed to create screen share: \(error)")
}
```

**Android:**
```kotlin
val request = ScreenShareRequest(
    communicationId = 123,
    communicationType = CommunicationType.CHAT,
    initiatedFrom = ScreenShareFrom.END_USER
)

val callbacks = ScreenShareCallbacks(
    onSessionStateChanged = { state ->
        Log.d("CCAI", "Screen share state changed: $state")
    },
    onSessionActivationRequest = {
        Log.d("CCAI", "Screen share activation requested")
        // Handle activation request
    },
    onSessionRemoteControlRequest = {
        Log.d("CCAI", "Remote control requested")
        // Handle remote control request
    },
    onSessionFullDeviceRequest = {
        Log.d("CCAI", "Full device access requested")
        // Handle full device access request
    },
    onSessionCreationError = { error ->
        Log.e("CCAI", "Screen share creation failed: ${error.message}")
    },
    onSessionDidSucceed = { response ->
        val cobrowseCode = response.code.data
        Log.d("CCAI", "Cobrowse code: $cobrowseCode")
    }
)

try {
    CCAI.screenShareService?.create(request, callbacks)
} catch (e: Exception) {
    Log.e("CCAI", "Failed to create screen share: ${e.message}")
}
```

### `activate`

**Method Signature:**

**iOS:**
```swift
func activate() async throws
```

**Android:**
```kotlin
suspend fun activate()
```

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.screenShareService?.activate()
    print("Screen share activated")
} catch {
    print("Failed to activate screen share: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.screenShareService?.activate()
    Log.d("CCAI", "Screen share activated")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to activate screen share: ${e.message}")
}
```

### `end`

**Method Signature:**

**iOS:**
```swift
func end() async throws
```

**Android:**
```kotlin
suspend fun end()
```

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.screenShareService?.end()
    print("Screen share ended")
} catch {
    print("Failed to end screen share: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.screenShareService?.end()
    Log.d("CCAI", "Screen share ended")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to end screen share: ${e.message}")
}
```

### `enableRemoteControl`

**Method Signature:**

**iOS:**
```swift
func enableRemoteControl(_ enable: Bool) async throws
```

**Android:**
```kotlin
suspend fun enableRemoteControl(enabled: Boolean)
```

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.screenShareService?.enableRemoteControl(true)
    print("Remote control enabled")
} catch {
    print("Failed to enable remote control: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.screenShareService?.enableRemoteControl(true)
    Log.d("CCAI", "Remote control enabled")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to enable remote control: ${e.message}")
}
```

### `enableFullDeviceSharing`

**Method Signature:**

**iOS:**
```swift
func enableFullDeviceSharing(_ enable: Bool) async throws
```

**Android:**
```kotlin
suspend fun enableFullDeviceSharing(enabled: Boolean)
```

**Error Handling:**
Throws on error; use `do/catch` in iOS or `try/catch` in Android.

**Example Usage:**

**iOS:**
```swift
do {
    try await CCAI.shared.screenShareService?.enableFullDeviceSharing(true)
    print("Full device sharing enabled")
} catch {
    print("Failed to enable full device sharing: \(error)")
}
```

**Android:**
```kotlin
try {
    CCAI.screenShareService?.enableFullDeviceSharing(true)
    Log.d("CCAI", "Full device sharing enabled")
} catch (e: Exception) {
    Log.e("CCAI", "Failed to enable full device sharing: ${e.message}")
}
```

### `clear`

**Method Signature:**

**iOS:**
```swift
func clear()
```

**Android:**
```kotlin
// Not available - Android does not have a clear method
```

**What it does:**
- Clears the screen share state and resets it to the ended state
- This method is only available on iOS

**Example Usage:**

**iOS:**
```swift
CCAI.shared.screenShareService?.clear()
print("Screen share state cleared")
```

**Android:**
```kotlin
// This method is not available on Android
// Use end() method instead to terminate the session
```

### `sessionState`

**Method Signature:**

**Android:**
```kotlin
fun sessionState(): ScreenShareSessionState
```

**Return Value:**
- Returns `ScreenShareSessionState` enum

**State Values:**
- `INACTIVE`: No active session
- `PENDING`: Session created, waiting for activation  
- `ACTIVE`: Session active and sharing screen

**Example Usage:**

**Android:**
```kotlin
val currentState = screenShareService?.sessionState()
when (currentState) {
    ScreenShareSessionState.INACTIVE -> Log.d("CCAI", "No session")
    ScreenShareSessionState.PENDING -> Log.d("CCAI", "Session pending")
    ScreenShareSessionState.ACTIVE -> Log.d("CCAI", "Session active")
}
```

### `isAvailable`

**Method Signature:**

**Android:**
```kotlin
fun isAvailable(): Boolean
```

**Return Value:**
- Returns `true` if screen sharing is available and properly configured
- Returns `false` if screen sharing is not available or not properly set up

**What it does:**
- Checks if the screen sharing functionality is available on the current device
- Verifies that the screen sharing module is properly imported and configured
- Recommended to call this method before using other screen share methods

**Example Usage:**

**Android:**
```kotlin
val isScreenShareAvailable = screenShareService?.isAvailable() ?: false
if (isScreenShareAvailable) {
    // Proceed with screen share operations
    screenShareService?.create(request, callbacks)
} else {
    Log.w("CCAI", "Screen sharing is not available")
}
```

### Screen Share State Management

The screen share service provides state management and event handling:

**State Enum:**
```swift
public enum ScreenShareServiceState {
    case none        // Default state when no session is active
    case pending     // Session is pending and waiting for authorization
    case authorizing // Session is being authorized
    case active      // Session is active and ready for use
    case ended       // Session has ended
}
```

**Request Types:**
```swift
public enum ScreenShareServiceRequest {
    case session        // A session request
    case remoteControl  // A remote control request
}
```

**State Monitoring:**

**iOS:**
```swift
// Option1. Using Combine
CCAI.shared.screenShareService?.statePublisher
    .sink { state in
        switch state {
        case .none:
            print("No screen share session")
        case .pending:
            print("Screen share pending")
        case .authorizing:
            print("Screen share authorizing")
        case .active:
            print("Screen share active")
        case .ended:
            print("Screen share ended")
        }
    }
    .store(in: &cancellables)

// Option2. Using delegate
CCAI.shared.screenShareService?.delegate = yourDelegate
```

**Android:**
```kotlin
// Define callbacks to handle screen share events
val callbacks = object : ScreenShareCallbacks {
    override val onSessionStateChanged: ((ScreenShareSessionState) -> Unit)? = { state ->
        when (state) {
            ScreenShareSessionState.INACTIVE -> Log.d("CCAI", "No screen share session")
            ScreenShareSessionState.PENDING -> Log.d("CCAI", "Screen share pending")
            ScreenShareSessionState.ACTIVE -> Log.d("CCAI", "Screen share active")
        }
    }
    
    // Other callback methods (onSessionDidSucceed, onSessionCreationError, etc.)
    // ...
}

// Create screen share session
val request = ScreenShareRequest(
    communicationId = 123,
    communicationType = CommunicationType.CHAT,
    initiatedFrom = ScreenShareFrom.END_USER
)

try {
    CCAI.screenShareService?.create(request, callbacks)
} catch (e: Exception) {
    Log.e("CCAI", "Failed to create screen share: ${e.message}")
}
```

### iOS SwiftUI Limitations

**Known SDK Limitations - iOS**

Remote control in Cobrowse iOS is currently not fully supported for SwiftUI-based components, which can cause certain buttons or interactions to be non-clickable. This is a known SDK limitation on iOS (due to SwiftUI), whereas UIKit-based components work as expected under remote control.

**Recommended Approaches for Remote Interaction in SwiftUI:**

1. **Wrap key interactive elements with UIKit**
   - For areas that need to be remotely clickable, wrap them as UIKit controls (e.g., UIButton) using `UIViewRepresentable` or `UIViewControllerRepresentable`
   - This ensures remote clicks work reliably via Cobrowse's UIKit path

2. **Forward remote touches with Custom Touch Handling**
   - For SwiftUI areas that can't be replaced with UIKit, use Cobrowse's custom touch callbacks to forward remote touch events to your SwiftUI logic
   - Implementation involves using a custom UIView or gesture recognizer to handle callbacks, then bridging events to SwiftUI (via UIViewRepresentable + Coordinator, notifications, or closures)

3. **Provide product-level guidance or fallback**
   - Clearly indicate on SwiftUI screens that "Agents can only view; user must tap locally"
   - Or automatically restrict remote-control permissions on these pages while maintaining view-only access

**Example Implementation for UIKit Wrapper:**

```swift
struct RemoteControlButton: UIViewRepresentable {
    let title: String
    let action: () -> Void
    
    func makeUIView(context: Context) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(title, for: .normal)
        button.addTarget(context.coordinator, action: #selector(Coordinator.buttonTapped), for: .touchUpInside)
        return button
    }
    
    func updateUIView(_ uiView: UIButton, context: Context) {
        uiView.setTitle(title, for: .normal)
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator(action: action)
    }
    
    class Coordinator: NSObject {
        let action: () -> Void
        
        init(action: @escaping () -> Void) {
            self.action = action
        }
        
        @objc func buttonTapped() {
            action()
        }
    }
}

// Usage in SwiftUI
struct ContentView: View {
    var body: some View {
        VStack {
            Text("This screen supports remote control")
            RemoteControlButton(title: "Clickable Button") {
                // Handle button tap
                print("Button tapped remotely or locally")
            }
        }
    }
}
```

## Custom Data

The SDK supports custom data that can be signed and unsigned.

### Signed Custom Data

**iOS:**
```swift
// JWT signed with company secret
let customData = CustomData(signed: "header.payload.signature")
```

**Android:**
```kotlin
// JWT signed with company secret
val customData = CustomData(
    signed = "header.payload.signature"
)
```

### Unsigned Custom Data

**iOS:**
```swift
var payload = CustomDataPayload()
payload["user_name"] = CustomDataItem(
    label: "Name", 
    value: "John"
)
payload["user_age"] = CustomDataItem(
    label: "Age", 
    value: 30, 
    type: .number, 
    invisibleToAgent: true
)

let customData = CustomData(unsigned: payload)
```

**Android:**
```kotlin
val payload = CustomDataPayload(
    user_name = CustomDataItem(
        label = "Name",
        value = "John"
    ),
    user_age = CustomDataItem(
        label = "Age",
        value = 30,
        type = CustomDataType.NUMBER,
        invisibleToAgent = true
    )
)

val customData = CustomData(
    unsigned = payload
)
```

### External Chat Transfer

**iOS:**
```swift
let transfer = ExternalChatTransfer(
    agent: ExternalChatAgent(name: "John Smith"),
    transcript: []
)
payload.externalChatTransfer = transfer
```

**Android:**
```kotlin
val transfer = ExternalChatTransfer(
    agent = ExternalChatAgent(
        name = "John Smith"
    ),
    transcript = emptyList()
)
payload.externalChatTransfer = transfer
```

## Authentication

### JWT Authentication

**iOS:**
```swift
do {
    let token = try await CCAI.shared.authService?.authenticate(jwtString)
    CCAI.shared.authService?.updateAuthToken(token)
} catch {
    // handle authentication error
}
```

**Android:**
```kotlin
try {
    val token = CCAI.authService?.authenticate(jwtString)
    CCAI.authService?.updateAuthToken(token)
} catch (e: Exception) {
    // handle authentication error
}
```

## Error Handling

All async methods in the CCAI SDK can throw errors. Always use proper error handling:

**iOS:**
```swift
do {
    let result = try await someAsyncMethod()
    // Handle success
} catch let error as CCAIError {
    // Handle CCAI specific errors
    print("CCAI Error: \(error.localizedDescription)")
} catch {
    // Handle other errors
    print("Error: \(error.localizedDescription)")
}
```

**Android:**
```kotlin
try {
    val result = someAsyncMethod()
    // Handle success
} catch (e: CCAIException) {
    // Handle CCAI specific errors
    Log.e("CCAI", "CCAI Error: ${e.message}")
} catch (e: Exception) {
    // Handle other errors
    Log.e("CCAI", "Error: ${e.message}")
}
```

## Threading Considerations

- All CCAI SDK methods are thread-safe and can be called from any thread
- Network operations are performed asynchronously
- UI updates should be performed on the main thread
- Combine publishers emit on the main thread by default (iOS)
- Flow collections should be collected on the main thread (Android)

## Combine Integration

The SDK provides reactive programming support:

**iOS (Combine):**
```swift
// Chat messages
CCAI.shared.chatService?.messagesReceivedSubject
    .sink { messages in
        // Handle new messages
    }
    .store(in: &cancellables)

// Screen share state
CCAI.shared.screenShareService?.statePublisher
    .sink { state in
        // Handle state changes
    }
    .store(in: &cancellables)
```

**Android (RxJava/Flow):**
```kotlin
// Chat messages
CCAI.chatService?.messagesReceivedSubject
    .collect { messages ->
        // Handle new messages
    }
```

## Memory Management

The SDK uses proper memory management and cleanup:

**iOS:**

- The CCAI SDK uses weak references for delegates to prevent retain cycles
- Services are automatically managed by the SDK
- Always store Combine cancellables to prevent memory leaks

```swift
private var cancellables = Set<AnyCancellable>()

deinit {
    cancellables.removeAll()
    CCAI.shared.delegate = nil
}

// Store cancellables
somePublisher
    .sink { value in
        // Handle value
    }
    .store(in: &cancellables)
```

**Android:**

- Services are automatically managed by the SDK
- Always cancel coroutines and flows in lifecycle methods
- Use weak references for listeners to prevent memory leaks

```kotlin
private var chatJob: Job? = null
private var screenShareJob: Job? = null

override fun onDestroy() {
    super.onDestroy()
    // Cancel all coroutine jobs
    chatJob?.cancel()
    screenShareJob?.cancel()
    // Clear any listeners or callbacks to prevent memory leaks
}

// Store coroutine jobs for chat events
chatJob = CoroutineScope(Dispatchers.Main).launch {
    CCAI.chatService?.messagesReceivedSubject?.collect { messages ->
        // Handle new messages
    }
}

// Store coroutine jobs for screen share events
screenShareJob = CoroutineScope(Dispatchers.Main).launch {
    // Monitor screen share state changes
    CCAI.screenShareService?.sessionState()?.let { state ->
        // Handle state changes
    }
}
```
