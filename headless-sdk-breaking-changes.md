# Breaking Changes: v3.0.0 → v3.1.0

This document outlines all breaking changes between 3.0.0 and 3.1.0 of the CCAI Mobile SDK.

**Legend:** 🤖 Android | 🍎 iOS

---

## 🔴 Breaking Changes Affecting Both Platforms

### Chat Service API Changes

#### 1. **Method Renamed: `start(menuId:)` → `start(request:)`**

**Platform:** 🤖 Android | 🍎 iOS

**Before (v3.0.0):**
```kotlin
// Android
suspend fun start(menuId: Int)
```
```swift
// iOS
func start(menuId: Int) async throws
```

**After (v3.1.0):**
```kotlin
// Android
suspend fun start(request: ChatRequest)
```
```swift
// iOS
func start(request: ChatRequest) async throws
```

**Migration:**
```kotlin
// Android - Before
chatService.start(menuId = 123)

// Android - After
val request = ChatRequest(
    chat = Chat(menuId = 123)
)
chatService.start(request)
```

```swift
// iOS - Before
try await chatService.start(menuId: 123)

// iOS - After
let request = ChatRequest(menuId: 123)
try await chatService.start(request: request)
```

---

#### 2. **ChatRequest Structure Changed**

**Platform:** 🤖 Android | 🍎 iOS

**Before (v3.0.0):**
```kotlin
// Android
data class ChatRequest(
    val menuId: Int,
    val languageCode: String = "en",
    val ticketId: String? = null,
    val greetingOverride: String? = null,
    val customData: CustomData? = null
)
```

**After (v3.1.0):**
```kotlin
// Android
data class ChatRequest(
    val chat: Chat? = null,
    val isScreenShareable: Boolean? = null
)

data class Chat(
    val menuId: Int = 0,
    val languageCode: String? = "en",
    val ticketId: String? = null,
    val greeting: String? = null,  // renamed from greetingOverride
    val customData: CustomData? = null,
    val displayOrderInAdapter: List<String>? = null  // new field
)
```

**Migration:**
```kotlin
// Android - Before
val request = ChatRequest(
    menuId = 123,
    greetingOverride = "Hello!"
)

// Android - After
val request = ChatRequest(
    chat = Chat(
        menuId = 123,
        greeting = "Hello!"
    )
)
```

```swift
// iOS - Before
let request = ChatRequest(
    menuId: 123,
    greetingOverride: "Hello!"
)

// iOS - After
let request = ChatRequest(
    menuId: 123,
    greeting: "Hello!"
)
```

---

### Company Service API Changes

#### **Method Renamed: `getCompany()` → `get()`**

**Platform:** 🤖 Android | 🍎 iOS

### **Method Renamed: `getCompany()` → `get()`**

**Platform:** Android, iOS

**Before (v3.0.0):**
```kotlin
// Android
suspend fun getCompany(): Company
```
```swift
// iOS
func getCompany() async throws -> CompanyResponse
```

**After (v3.1.0):**
```kotlin
// Android
suspend fun get(): Company
```
```swift
// iOS
func get() async throws -> CompanyResponse
```

**Migration:**
```kotlin
// Android - Before
val company = companyService.getCompany()

// Android - After
val company = companyService.get()
```

```swift
// iOS - Before
let company = try await companyService.getCompany()

// iOS - After
let company = try await companyService.get()
```

---

### Queue Menu Service API Changes

#### 1. **Method Renamed: `getMenus()` → `get()`**

**Platform:** 🤖 Android | 🍎 iOS

**Before (v3.0.0):**
```kotlin
// Android
suspend fun getMenus(key: String? = null): MenuResult
```
```swift
// iOS
func getMenus(key: String?, language: String?) async throws -> ([QueueMenu], Bool)
```

**After (v3.1.0):**
```kotlin
// Android
suspend fun get(key: String? = null): MenuResult
```
```swift
// iOS
func get(key: String?, language: String?) async throws -> ([QueueMenu], Bool)
```

**Migration:**
```kotlin
// Android - Before
val menus = queueMenuService.getMenus()

// Android - After
val menus = queueMenuService.get()
```

```swift
// iOS - Before
let (menus, isDirect) = try await queueMenuService.getMenus(key: nil, language: "en")

// iOS - After
let (menus, isDirect) = try await queueMenuService.get(key: nil, language: "en")
```

---

#### 2. **Method Renamed: `getMenuWaitTime()` → `getWaitTimes()`**

**Platform:** 🤖 Android | 🍎 iOS

**Before (v3.0.0):**
```kotlin
// Android
suspend fun getMenuWaitTime(menuId: Int): QueueMenuWaitTimeResponse
```
```swift
// iOS
func getMenuWaitTime(menuId: Int, language: String?) async throws -> QueueMenuWaitTimeResponse
```

**After (v3.1.0):**
```kotlin
// Android
suspend fun getWaitTimes(menuId: Int): QueueMenuWaitTimeResponse
```
```swift
// iOS
func getWaitTimes(menuId: Int, language: String?) async throws -> QueueMenuWaitTimeResponse
```

**Migration:**
```kotlin
// Android - Before
val waitTime = queueMenuService.getMenuWaitTime(menuId = 123)

// Android - After
val waitTime = queueMenuService.getWaitTimes(menuId = 123)
```

```swift
// iOS - Before
let waitTime = try await queueMenuService.getMenuWaitTime(menuId: 123, language: nil)

// iOS - After
let waitTime = try await queueMenuService.getWaitTimes(menuId: 123, language: nil)
```

---

### Model Changes

#### **ChatOptions Extended**

**Platform:** 🤖 Android | 🍎 iOS

**New properties added:**

```kotlin
// Android
data class ChatOptions(
    val messagesPaginationSize: Int = 100,
    val downloadTranscriptMaxRetries: Int = 5,  // NEW
    val downloadTranscriptMaxDelay: Long = 3000L,  // NEW
    val webFormInterface: CCAIWebFormInterface? = null,
    var downloadTranscriptVisibility: DownloadTranscriptVisibility = DownloadTranscriptVisibility.SHOW_ALL,  // NEW
    var greeting: String? = null  // NEW
)
```

```swift
// iOS
public struct ChatOptions {
    public var messagesPaginationSize: UInt = 100
    public var downloadTranscriptMaxRetries: Int = 3  // NEW
    public var downloadTranscriptMaxDelay: TimeInterval = 5  // NEW
    public var downloadTranscriptVisibility: DownloadTranscriptVisibility = .showAll  // NEW
    public var greeting: String?  // NEW
    public var delegate: CCAIChatDelegate?
}
```

---

## 🟠 Breaking Changes - Android Only

### Chat Service API Changes

#### 1. **Method Renamed: `resume(chat:)` → `resumeChat(chat:)`**

**Platform:** 🤖 Android

**Before (v3.0.0):**
```kotlin
suspend fun resume(chat: ChatResponse)
```

**After (v3.1.0):**
```kotlin
suspend fun resumeChat(chat: ChatResponse)
```

**Migration:**
```kotlin
// Before
chatService.resume(chat)

// After
chatService.resumeChat(chat)
```

---

#### 2. **Method Renamed: `getFormUrl()` → `fetchWebForm()`**

**Platform:** 🤖 Android

**Before (v3.0.0):**
```kotlin
suspend fun getFormUrl(externalFormId: String, smartActionId: Int): WebFormResponse
```

**After (v3.1.0):**
```kotlin
suspend fun fetchWebForm(externalFormId: String, smartActionId: Int): WebFormResponse
```

**Migration:**
```kotlin
// Before
val webForm = chatService.getFormUrl(formId, actionId)

// After
val webForm = chatService.fetchWebForm(formId, actionId)
```

---

#### 3. **Method Renamed: `verifyFormData()` → `validateWebForm()`**

**Platform:** 🤖 Android

**Before (v3.0.0):**
```kotlin
suspend fun verifyFormData(formData: WebFormResponse): Boolean
```

**After (v3.1.0):**
```kotlin
suspend fun validateWebForm(formData: WebFormResponse): Boolean
```

**Migration:**
```kotlin
// Before
val isValid = chatService.verifyFormData(formData)

// After
val isValid = chatService.validateWebForm(formData)
```

---

#### 4. **Method Renamed: `getPreviousChats()` → Parameter Name Changed**

**Platform:** 🤖 Android

**Before (v3.0.0):**
```kotlin
suspend fun getPreviousChats(page: Int, perPage: Int): ChatHistoryInfo?
```

**After (v3.1.0):**
```kotlin
suspend fun getPreviousChats(currentPage: Int): PreviousChat?
```

**Migration:**
```kotlin
// Before
val history = chatService.getPreviousChats(page = 1, perPage = 20)

// After
val history = chatService.getPreviousChats(currentPage = 1)
```

---

#### 5. **Method Return Type Changed: `endChat()`**

**Platform:** 🤖 Android

**Before (v3.0.0):**
```kotlin
suspend fun endChat(): Boolean
```

**After (v3.1.0):**
```kotlin
suspend fun endChat()  // Returns Unit, throws on error
```

**Migration:**
```kotlin
// Before
val success = chatService.endChat()
if (success) {
    // Handle success
}

// After
try {
    chatService.endChat()
    // Handle success
} catch (e: Exception) {
    // Handle error
}
```

---

#### 6. **Removed Methods: Task VA Related**

**Platform:** 🤖 Android

The following Task VA methods have been **removed**:

```kotlin
// REMOVED in v3.1.0
suspend fun getTaskVaMessage(messageId: Int?): ChatTaskVaMessage?
suspend fun sendTaskVaMessage(message: OutgoingMessageContent): ChatTaskVaMessage?
suspend fun uploadPhotos(photos: List<ByteArray>, smartAction: SmartAction?): List<Int>
suspend fun getTaskVaMessages(): List<ChatMessage>?
```

**Migration:** These methods are no longer available. Task VA functionality is now handled internally by the SDK.

---

### Model Changes

#### 1. **Model Renamed: `ChatHistoryInfo` → `PreviousChat`**

**Platform:** 🤖 Android

**Before (v3.0.0):**
```kotlin
data class ChatHistoryInfo(
    val chatMessages: List<ChatMessage>,
    val agents: List<Agent>,
    val nextPage: Int?
)
```

**After (v3.1.0):**
```kotlin
data class PreviousChat(
    val messages: List<ChatMessage>,  // renamed from chatMessages
    val agents: List<Agent>,
    val nextPage: Int?
)
```

**Migration:**
```kotlin
// Before
val history: ChatHistoryInfo? = chatService.getPreviousChats(page = 1, perPage = 20)
val chatMessages = history?.chatMessages.orEmpty()

// After
val history: PreviousChat? = chatService.getPreviousChats(currentPage = 1)
val chatMessages = history?.messages.orEmpty()
```

**Impact:**
- Model renamed: `ChatHistoryInfo` → `PreviousChat`
- Property renamed: `chatMessages` → `messages`

---

## 🟣 Breaking Changes - iOS Only

### Chat Service API Changes

#### **Method Return Type Changed: `submitCustomForm()`**

**Platform:** 🍎 iOS

**Before (v3.0.0):**
```swift
@discardableResult
func submitCustomForm(request: SubmitCustomFormRequest) async throws -> SubmitCustomFormResponse
```

**After (v3.1.0):**
```swift
func submitCustomForm(request: SubmitCustomFormRequest) async throws
```

**Migration:**
```swift
// Before
let response = try await chatService.submitCustomForm(request: request)

// After
try await chatService.submitCustomForm(request: request)
```

---

## 📋 Migration Summary

### Both Platforms (🤖 Android | 🍎 iOS)

- `chatService.start(menuId)` → `chatService.start(request)` with `ChatRequest` object
- `ChatRequest.greetingOverride` → `ChatRequest.greeting`
- `companyService.getCompany()` → `companyService.get()`
- `queueMenuService.getMenus()` → `queueMenuService.get()`
- `queueMenuService.getMenuWaitTime()` → `queueMenuService.getWaitTimes()`
- Update `ChatOptions` to include new properties (transcript download settings, greeting)

### Android Only (🤖)

- `chatService.resume()` → `chatService.resumeChat()`
- `chatService.getFormUrl()` → `chatService.fetchWebForm()`
- `chatService.verifyFormData()` → `chatService.validateWebForm()`
- `chatService.getPreviousChats(page, perPage)` → `chatService.getPreviousChats(currentPage)`
- Update `ChatRequest` structure to use nested `Chat` object
- `ChatHistoryInfo` → `PreviousChat` (also rename property `chatMessages` → `messages`)
- Update `endChat()` error handling (now throws instead of returning Boolean)
- Remove usage of Task VA methods: `getTaskVaMessage()`, `sendTaskVaMessage()`, `uploadPhotos()`, `getTaskVaMessages()`

### iOS Only (🍎)

- Update `submitCustomForm()` to not expect return value

---

**Last Updated:** 2025-10-07
**SDK Version:** v3.1.0
