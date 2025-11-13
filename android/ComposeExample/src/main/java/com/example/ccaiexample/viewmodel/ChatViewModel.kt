package com.example.ccaiexample.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ccaiplatform.ccaichat.chatService
import com.ccaiplatform.ccaichat.model.ChatMessage
import com.ccaiplatform.ccaichat.model.ChatResponse
import com.ccaiplatform.ccaichat.model.enum.ChatMemberEvent
import com.ccaiplatform.ccaichat.model.enum.ChatProviderState
import com.ccaiplatform.ccaichat.model.enum.ChatStatus
import com.ccaiplatform.ccaichat.model.enum.ChatTypingEvent
import com.ccaiplatform.ccaichat.model.enum.OutgoingMessageContent
import com.ccaiplatform.ccaichat.service.ChatServiceInterface
import com.ccaiplatform.ccaikit.CCAI
import com.ccaiplatform.ccaikit.models.logger.LogLevel
import com.ccaiplatform.ccaikit.models.response.communication.Agent
import com.ccaiplatform.ccaikit.util.logging.LoggingUtil
import co.ccai.example.compose_example.R
import com.ccaiplatform.ccaichat.model.Chat
import com.ccaiplatform.ccaichat.model.ChatRequest
import com.ccaiplatform.ccaichat.model.enum.ChatMessageEvent
import com.ccaiplatform.ccaikit.ScreenShareManager
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogOperationType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType
import com.ccaiplatform.ccaikit.models.screenShare.ScreenShareResponse
import com.example.shared.ScreenShareController
import com.example.shared.ScreenShareControllerImpl
import com.example.shared.ScreenShareEligibility
import com.example.shared.ScreenShareToggleResult
import com.example.shared.model.Message
import com.example.shared.model.Message.User
import com.example.shared.model.MessageType
import com.example.shared.model.UserRole
import com.example.shared.model.convertToDisplayMessageModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private var menuId: Int = -1

    private val service: ChatServiceInterface? = CCAI.chatService

    var messages by mutableStateOf<List<Message>>(emptyList())
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    var state by mutableStateOf<ChatProviderState>(ChatProviderState.None)
        private set

    var isTyping by mutableStateOf(false)
        private set

    var chatStatus by mutableStateOf<ChatStatus?>(null)
        private set

    var chat by mutableStateOf<ChatResponse?>(null)
        private set

    var showEndingDialog: Boolean by mutableStateOf(false)
        private set

    var isSending by mutableStateOf(false)
        private set

    var currentAgent by mutableStateOf<Agent?>(null)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isScreenShareEnabled by mutableStateOf(false)
        private set

    var currentScreenShareDialogType: ScreenShareDialogType? by mutableStateOf(null)
        private set

    private val application = getApplication<Application>()
    private var participants: MutableMap<String, User> = mutableMapOf()
    private var currentHistoryPageNumber = 1

    private val screenShareController: ScreenShareController by lazy {
        ScreenShareControllerImpl(
            onShowDialog = { dialogType ->
                currentScreenShareDialogType = dialogType
            },
            onSendMessage = this::sendScreenShareMessage
        )
    }

    init {
        setupStateSubscriptions()
        setupMessageSubscriptions()
    }

    fun startChat() {
        launchWithErrorHandling(
            onError = { throwable ->
                Log.e(
                    "ChatViewModel",
                    "Failed to start chat: ${throwable.localizedMessage}",
                    throwable
                )
                errorMessage = application.getString(
                    R.string.failed_to_start_chat,
                    throwable.localizedMessage
                )
            }) {
            service?.start(
                ChatRequest(
                    chat = Chat(menuId),
                    isScreenShareable = ScreenShareManager.isAvailable()
                )
            )
        }
    }

    fun resumeChat(chatResponse: ChatResponse) {
        launchWithErrorHandling(
            onError = { throwable ->
                Log.e(
                    "ChatViewModel",
                    "Failed to start chat: ${throwable.localizedMessage}",
                    throwable
                )
                errorMessage = application.getString(
                    R.string.failed_to_start_chat,
                    throwable.localizedMessage
                )
            }) {
            service?.resumeChat(chatResponse)
        }
    }

    fun endChat(onChatEndSuccess: () -> Unit) {
        launchWithErrorHandling(
            onError = { throwable ->
                errorMessage = application.getString(R.string.end_chat_fail)
                LoggingUtil.log("Failed to end chat: ${throwable.localizedMessage}", LogLevel.ERROR)
                showEndingDialog = false
            }) {
            showEndingDialog = true
            runCatching {
                service?.endChat()
            }.onSuccess {
                onChatEndSuccess()
            }.onFailure { e ->
                errorMessage = application.getString(R.string.end_chat_fail)
            }
            showEndingDialog = false
        }
    }

    private fun sendMessage(message: OutgoingMessageContent) {
        isSending = true

        // Create end user if not exists
        val endUserId = "end_user"
        val endUser = participants[endUserId]
            ?: User(id = endUserId, name = "You", role = UserRole.CURRENT_USER)
        participants[endUserId] = endUser

        val messagesToSend = message.convertToDisplayMessageModel(endUser)
        messages = messages + messagesToSend

        launchWithErrorHandling(onError = { throwable ->
            LoggingUtil.log("Error: ${throwable.localizedMessage}", LogLevel.ERROR)
            isSending = false
        }) {
            service?.sendMessage(message)
            isSending = false
        }
    }

    fun sendTextMessage(inputText: String) {
        val trimmedInputText = inputText.trim()
        if (trimmedInputText.isEmpty()) return

        val content = OutgoingMessageContent.Text(trimmedInputText)
        sendMessage(content)
    }

    fun sendPhotoMessage(data: ByteArray, uri: Uri) {
        val content = OutgoingMessageContent.Photos(photos = listOf(data), uris = listOf(uri))
        sendMessage(content)
    }

    fun sendScreenShareMessage(
        event: ChatMessageEvent,
        payload: ScreenShareResponse? = null
    ) {
        val message = OutgoingMessageContent.ScreenShare(event.value, payload)
        sendMessage(message)
    }

    fun refreshMessages() {
        isRefreshing = true
        launchWithErrorHandling(
            onError = { throwable ->
                errorMessage = application.getString(R.string.failed_to_refresh_messages)
                isRefreshing = false
            }
        ) {
            loadHistoryMessages()
            isRefreshing = false
        }
    }

    private suspend fun loadHistoryMessages(): Int {
        runCatching {
            if (currentHistoryPageNumber > 0) {
                service?.getPreviousChats(
                    currentHistoryPageNumber
                )
            } else {
                null
            }
        }.onSuccess { chatHistoryInfo ->
            val chatMessages = chatHistoryInfo?.messages.orEmpty()
            val agents = chatHistoryInfo?.agents.orEmpty()
            val nextPage = chatHistoryInfo?.nextPage ?: -1
            currentHistoryPageNumber = nextPage

            handleAgents(agents)

            val historyMessages = chatMessages.map {
                handleChatMessages(it)
            }.let { messages ->
                messages.filter { it != null }.map { it!! }
            }
            messages = historyMessages + messages
            return messages.size
        }.onFailure { e ->
            LoggingUtil.log(
                message = "Failed to load history messages: ${e.localizedMessage}",
                level = LogLevel.ERROR,
                throwable = e
            )
        }
        return 0
    }

    private fun setupMessageSubscriptions() {
        viewModelScope.launch {
            val messagesReceivedJob = launch {
                service?.messagesReceivedSubject?.collect { chatMessages ->
                    val messagesValue = chatMessages
                        .map { message: ChatMessage ->
                            Log.d("ChatViewModel", "Received message: $message")
                            handleChatMessages(message)
                        }
                        .let { messages ->
                            messages.filter { it != null }.map { it!! }
                        }
                    messages = messages + messagesValue

                    // Handle agent screen share request if needed
                    screenShareController.handleAgentRequestIfNeeded(chatMessages)
                }
            }

            messagesReceivedJob.join()
        }
    }

    private fun setupStateSubscriptions() {
        viewModelScope.launch {
            val stateChangeJob = launch {
                service?.stateChangedSubject?.distinctUntilChanged()
                    ?.collect { newState ->
                        state = newState

                        launchWithErrorHandling {
                            if (newState == ChatProviderState.Connecting) {
                                loadHistoryMessages()
                            }
                        }

                        launchWithErrorHandling {
                            service.checkStatus()
                        }
                    }
            }
            val typingEventJob = launch {
                service?.typingEventSubject?.collect { event ->
                    isTyping = when (event) {
                        is ChatTypingEvent.Started -> true
                        is ChatTypingEvent.Ended -> false
                    }
                }
            }
            val memberEventJob = launch {
                service?.memberEventSubject?.collect { event ->
                    when (event) {
                        is ChatMemberEvent.Joined -> {
                            Log.d("ChatViewModel", "${event.identity ?: ""} joined")
                        }

                        is ChatMemberEvent.Left -> {
                            Log.d("ChatViewModel", "${event.identity ?: ""} left")
                            if (event.identity != null) {
                                participants.remove(event.identity)
                            }
                        }
                    }
                    launchWithErrorHandling {
                        service.checkStatus()
                    }
                }
            }
            val chatReceivedJob = launch {
                service?.chatReceivedSubject?.collect { _chat ->
                    chat = _chat
                    handleChatUpdate(_chat)
                }
            }
            stateChangeJob.join()
            typingEventJob.join()
            memberEventJob.join()
            chatReceivedJob.join()
        }
    }

    private fun handleAgents(agents: List<Agent>) {
        agents.forEach { agent ->
            val agentId = agent.agentIdString ?: return@forEach
            if (participants[agentId] == null) {
                participants[agentId] = User(
                    id = agentId,
                    name = agent.displayName ?: "Agent",
                    role = UserRole.SYSTEM
                )
            }
        }
    }

    private fun handleChatMessages(chatMessage: ChatMessage): Message? {
        val content = chatMessage.body.content ?: return null
        val authorId = chatMessage.author ?: return null
        val user = userFromMessageAuthorId(authorId)
        return Message(
            text = content,
            user = user,
            createdAt = chatMessage.date,
            type = MessageType.fromValue(chatMessage.body.type.value)
        )
    }

    private fun userFromMessageAuthorId(id: String): User {
        val user = participants[id]
        if (user != null) {
            return user
        } else {
            val isEndUser = id.startsWith("end_user")
            val name = if (isEndUser) "You" else "System"
            val type = if (isEndUser) UserRole.CURRENT_USER else UserRole.SYSTEM
            val newUser = User(id = id, name = name, role = type)
            participants[id] = newUser
            return newUser
        }
    }

    private fun handleChatUpdate(chat: ChatResponse?) {
        chatStatus = chat?.status
        isScreenShareEnabled =
            ScreenShareEligibility.isEnabled(chatStatus, chat?.supportScreenShare)
        val agent = chat?.currentAgent ?: return
        val agentId = agent.agentIdString ?: return

        if (currentAgent?.agentIdString != agentId) {
            currentAgent = agent
        }

        if (participants[agentId] == null) {
            participants[agentId] = User(
                id = agentId,
                name = agent.displayName ?: "Agent",
                role = UserRole.SYSTEM
            )
        }
    }

    private fun launchWithErrorHandling(
        onError: (Throwable) -> Unit = { e ->
            LoggingUtil.log("Error: ${e.localizedMessage}", LogLevel.ERROR)
        },
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                block()
            }.onFailure {
                onError(it)
            }
        }
    }

    fun updateMenuId(menuId: Int) {
        this.menuId = menuId
    }

    fun toggleScreenShare() {
        val result = screenShareController.toggle(chatStatus, chat?.supportScreenShare)
        when (result) {
            is ScreenShareToggleResult.Failed -> {
                errorMessage = result.reason
            }

            is ScreenShareToggleResult.Success -> {
                LoggingUtil.log("Screen share toggle successful: ${result.state}", LogLevel.DEBUG)
            }
        }
    }

    fun handleScreenShareDialogConfirm(
        actionType: ScreenShareDialogType,
        operationType: ScreenShareDialogOperationType
    ) {
        screenShareController.processScreenShareDialogResponse(actionType, operationType, chat?.id)
    }

    fun handleScreenShareDialogDismiss() {
        currentScreenShareDialogType = null
    }
}
