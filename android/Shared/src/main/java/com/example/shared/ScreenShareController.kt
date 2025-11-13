package com.example.shared

import com.ccaiplatform.ccaichat.model.ChatMessage
import com.ccaiplatform.ccaichat.model.enum.ChatStatus
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogOperationType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareSessionState

sealed class ScreenShareToggleResult {
    data class Success(val state: ScreenShareSessionState) : ScreenShareToggleResult()
    data class Failed(val reason: String) : ScreenShareToggleResult()
}

interface ScreenShareController {
    // Toggles screen share based on current chat status and support
    fun toggle(chatStatus: ChatStatus?, supportsScreenShare: Boolean?): ScreenShareToggleResult

    // Checks chat messages for agent-initiated screen share requests and handles them
    fun handleAgentRequestIfNeeded(chatMessages: List<ChatMessage>)

    // Processes user responses from screen share dialogs
    fun processScreenShareDialogResponse(
        actionType: ScreenShareDialogType,
        operationType: ScreenShareDialogOperationType,
        chatId: Int?
    )
}
