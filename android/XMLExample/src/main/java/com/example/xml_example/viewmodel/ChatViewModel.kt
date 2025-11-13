package com.example.xml_example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import co.ccai.example.xml_example.R
import com.ccaiplatform.ccaichat.chatService
import com.ccaiplatform.ccaichat.model.Chat
import com.ccaiplatform.ccaichat.model.ChatMessage
import com.ccaiplatform.ccaichat.model.ChatRequest
import com.ccaiplatform.ccaichat.model.ChatResponse
import com.ccaiplatform.ccaichat.model.enum.ChatMemberEvent
import com.ccaiplatform.ccaichat.model.enum.ChatMessageEvent
import com.ccaiplatform.ccaichat.model.enum.ChatProviderState
import com.ccaiplatform.ccaichat.model.enum.ChatStatus
import com.ccaiplatform.ccaichat.model.enum.ChatTypingEvent
import com.ccaiplatform.ccaichat.model.enum.OutgoingMessageContent
import com.ccaiplatform.ccaichat.service.ChatServiceInterface
import com.ccaiplatform.ccaikit.CCAI
import com.ccaiplatform.ccaikit.ScreenShareManager
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogOperationType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareSessionState
import com.ccaiplatform.ccaikit.models.logger.LogLevel
import com.ccaiplatform.ccaikit.models.response.communication.Agent
import com.ccaiplatform.ccaikit.models.screenShare.ScreenShareResponse
import com.ccaiplatform.ccaikit.util.logging.LoggingUtil
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

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private var menuId: Int = -1
    private val service: ChatServiceInterface? = CCAI.chatService

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _state = MutableLiveData<ChatProviderState>(ChatProviderState.None)
    val state: LiveData<ChatProviderState> = _state

    private val _isTyping = MutableLiveData<Boolean>(false)
    val isTyping: LiveData<Boolean> = _isTyping

    private val _chatStatus = MutableLiveData<ChatStatus?>()
    val chatStatus: LiveData<ChatStatus?> = _chatStatus

    private val _chat = MutableLiveData<ChatResponse?>()
    val chat: LiveData<ChatResponse?> = _chat

    private val _showLoadingDialog = MutableLiveData<Pair<Boolean, String>>(false to "")
    val showLoadingDialog: LiveData<Pair<Boolean, String>> = _showLoadingDialog

    private val _isSending = MutableLiveData<Boolean>(false)
    val isSending: LiveData<Boolean> = _isSending

    private val _isRefreshing = MutableLiveData<Boolean>(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _isScreenShareEnabled = MutableLiveData<Boolean>(false)
    val isScreenShareEnabled: LiveData<Boolean> = _isScreenShareEnabled

    private val _screenShareSessionState = MutableLiveData<ScreenShareSessionState>()
    val screenShareSessionState: LiveData<ScreenShareSessionState> = _screenShareSessionState

    private val _currentScreenShareDialogType = MutableLiveData<ScreenShareDialogType?>()
    val currentScreenShareDialogType: LiveData<ScreenShareDialogType?> = _currentScreenShareDialogType

    private val application = getApplication<Application>()
    private var participants: MutableMap<String, User> = mutableMapOf()
    private var currentHistoryPageNumber = 1
    private var screenShareListener: ((ScreenShareSessionState) -> Unit)? = null

    private val screenShareController: ScreenShareController by lazy {
        ScreenShareControllerImpl(
            onShowDialog = { dialogType ->
                _currentScreenShareDialogType.value = dialogType
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
                _errorMessage.value = application.getString(
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
                _errorMessage.value = application.getString(
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
                _errorMessage.value = application.getString(R.string.end_chat_fail)
                LoggingUtil.log("Failed to end chat: ${throwable.localizedMessage}", LogLevel.ERROR)
                _showLoadingDialog.value = false to ""
            }) {
            _showLoadingDialog.value = true to application.getString(R.string.ending_chat)
            runCatching {
                service?.endChat()
            }.onSuccess {
                onChatEndSuccess()
            }.onFailure { e ->
                _errorMessage.value = application.getString(R.string.end_chat_fail)
            }
            _showLoadingDialog.value = false to ""
        }
    }

    private fun sendMessage(message: OutgoingMessageContent) {
        _isSending.value = true

        // Create end user if not exists
        val endUserId = "end_user"
        val endUser = participants[endUserId]
            ?: User(id = endUserId, name = "You", role = UserRole.CURRENT_USER)
        participants[endUserId] = endUser

        val messagesToSend = message.convertToDisplayMessageModel(endUser)
        _messages.value = _messages.value?.plus(messagesToSend)

        launchWithErrorHandling(onError = { throwable ->
            LoggingUtil.log("Error: ${throwable.localizedMessage}", LogLevel.ERROR)
            _isSending.value = false
        }) {
            service?.sendMessage(message)
            _isSending.value = false
        }
    }

    fun sendTextMessage(inputText: String) {
        val trimmedInputText = inputText.trim()
        if (trimmedInputText.isEmpty()) return

        sendMessage(OutgoingMessageContent.Text(trimmedInputText))
    }

    fun sendPhotoMessage(data: ByteArray, uri: Uri) {
        sendMessage(OutgoingMessageContent.Photos(photos = listOf(data), uris = listOf(uri)))
    }

    fun sendScreenShareMessage(
        event: ChatMessageEvent,
        payload: ScreenShareResponse? = null
    ) {
        launchWithErrorHandling(
            onError = { throwable ->
                LoggingUtil.log(
                    "Error sending screen share message [${event.value}, payload=$payload]: ${throwable.localizedMessage}",
                    LogLevel.ERROR
                )
            }
        ) {
            sendMessage(OutgoingMessageContent.ScreenShare(event.value, payload))
        }
    }

    fun refreshMessages() {
        _isRefreshing.value = true
        launchWithErrorHandling(
            onError = { throwable ->
                _errorMessage.value = application.getString(R.string.failed_to_refresh_messages)
                _isRefreshing.value = false
            }
        ) {
            loadHistoryMessages()
            _isRefreshing.value = false
        }
    }

    private suspend fun loadHistoryMessages() {
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
            val currentMessages = _messages.value ?: emptyList()
            val newMessages = historyMessages + currentMessages
            _messages.value = newMessages
        }.onFailure { e ->
            LoggingUtil.log(
                message = "Failed to load history messages: ${e.localizedMessage}",
                level = LogLevel.ERROR,
                throwable = e
            )
        }
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
                    _messages.value = _messages.value?.plus(messagesValue)

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
                        _state.value = newState
                        _showLoadingDialog.value = Pair(
                            newState != ChatProviderState.Connected,
                            application.getString(R.string.connecting)
                        )

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
                    _isTyping.value = when (event) {
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
                service?.chatReceivedSubject?.collect { newChat ->
                    _chat.value = newChat
                    handleChatUpdate(newChat)
                }
            }
            stateChangeJob.join()
            typingEventJob.join()
            memberEventJob.join()
            chatReceivedJob.join()
        }

        // Setup screen share state listener
        setupScreenShareListener()
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
        _chatStatus.value = chat?.status
        _isScreenShareEnabled.value =
            ScreenShareEligibility.isEnabled(_chatStatus.value, chat?.supportScreenShare)
        val agent = chat?.currentAgent ?: return
        val agentId = agent.agentIdString ?: return

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
        val result =
            screenShareController.toggle(_chatStatus.value, _chat.value?.supportScreenShare)
        when (result) {
            is ScreenShareToggleResult.Failed -> {
                _errorMessage.value = result.reason
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
        screenShareController.processScreenShareDialogResponse(actionType, operationType, _chat.value?.id)
    }

    fun handleScreenShareDialogDismiss() {
        _currentScreenShareDialogType.value = null
    }

    private fun setupScreenShareListener() {
        // Get current state
        ScreenShareManager.getSessionState()?.let { state ->
            _screenShareSessionState.value = state
        }

        // Setup listener for state changes
        val listener = { state: ScreenShareSessionState ->
            _screenShareSessionState.value = state
        }
        screenShareListener = listener
        ScreenShareManager.addStateChangeListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        screenShareListener?.let { ScreenShareManager.removeStateChangeListener(it) }
    }
}
