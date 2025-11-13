package com.example.shared

import com.ccaiplatform.ccaichat.model.ChatMessage
import com.ccaiplatform.ccaichat.model.enum.ChatMessageEvent
import com.ccaiplatform.ccaichat.model.enum.ChatStatus
import com.ccaiplatform.ccaikit.ScreenShareManager
import com.ccaiplatform.ccaikit.interfaces.ScreenShareCallbacks
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogOperationType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareSessionState
import com.ccaiplatform.ccaikit.models.enums.CommunicationType
import com.ccaiplatform.ccaikit.models.logger.LogLevel
import com.ccaiplatform.ccaikit.models.screenShare.ScreenShareFrom
import com.ccaiplatform.ccaikit.models.screenShare.ScreenShareRequest
import com.ccaiplatform.ccaikit.models.screenShare.ScreenShareResponse
import com.ccaiplatform.ccaikit.util.logging.LoggingUtil
import com.example.shared.extensions.findScreenShareRequest

class ScreenShareControllerImpl(
    private val onShowDialog: (ScreenShareDialogType) -> Unit,
    private val onSendMessage: (ChatMessageEvent, ScreenShareResponse?) -> Unit
) : ScreenShareController {

    private var screenShareInitiatedFrom: ScreenShareFrom? = null

    override fun toggle(
        chatStatus: ChatStatus?,
        supportsScreenShare: Boolean?
    ): ScreenShareToggleResult {
        if (!computeEnabled(chatStatus, supportsScreenShare)) {
            LoggingUtil.log("Cannot start screen share", LogLevel.WARN)
            return ScreenShareToggleResult.Failed("Screen share is not enabled for current chat status")
        }

        val currentState = ScreenShareManager.getSessionState()
        return when (currentState) {
            ScreenShareSessionState.INACTIVE -> {
                onShowDialog(ScreenShareDialogType.USER_SCREEN_SHARE_REQUEST)
                ScreenShareToggleResult.Success(currentState)
            }

            ScreenShareSessionState.PENDING -> {
                LoggingUtil.log("Screen share is already starting", LogLevel.DEBUG)
                ScreenShareToggleResult.Failed("Screen share is already starting")
            }

            ScreenShareSessionState.ACTIVE -> {
                onShowDialog(ScreenShareDialogType.STOP_CONFIRMATION)
                ScreenShareToggleResult.Success(currentState)
            }

            null -> {
                LoggingUtil.log("Screen share state is null", LogLevel.WARN)
                ScreenShareToggleResult.Failed("Screen share service is not available")
            }
        }
    }

    override fun handleAgentRequestIfNeeded(chatMessages: List<ChatMessage>) {
        if (!chatMessages.findScreenShareRequest()) return

        if (ScreenShareManager.getSessionState() == ScreenShareSessionState.ACTIVE) {
            LoggingUtil.log("Screen share is already active, skipping", LogLevel.DEBUG)
            return
        }
        onShowDialog(ScreenShareDialogType.AGENT_SCREEN_SHARE_REQUEST)
    }

    override fun processScreenShareDialogResponse(
        actionType: ScreenShareDialogType,
        operationType: ScreenShareDialogOperationType,
        chatId: Int?
    ) {
        when (actionType) {
            ScreenShareDialogType.USER_SCREEN_SHARE_REQUEST -> handleUserScreenShareRequest(
                operationType, chatId
            )

            ScreenShareDialogType.AGENT_SCREEN_SHARE_REQUEST -> handleAgentScreenShareRequest(
                operationType, chatId
            )

            ScreenShareDialogType.ACTIVATION_REQUEST -> handleActivationRequest(operationType)
            ScreenShareDialogType.REMOTE_CONTROL_REQUEST -> handleRemoteControlRequest(operationType)
            ScreenShareDialogType.FULL_DEVICE_REQUEST -> handleFullDeviceRequest(operationType)
            ScreenShareDialogType.STOP_CONFIRMATION -> handleStopConfirmation(operationType)
        }
    }

    private fun computeEnabled(chatStatus: ChatStatus?, supportsScreenShare: Boolean?): Boolean {
        return ScreenShareEligibility.isEnabled(chatStatus, supportsScreenShare)
    }

    private fun stop() {
        ScreenShareManager.stopSession()
        screenShareInitiatedFrom = null
    }

    private fun handleUserScreenShareRequest(
        operationType: ScreenShareDialogOperationType,
        chatId: Int?
    ) {
        when (operationType) {
            ScreenShareDialogOperationType.CONFIRM -> requestScreenShare(
                isFromRemote = false,
                chatId = chatId
            )

            ScreenShareDialogOperationType.CANCEL -> LoggingUtil.log(
                "User cancelled screen share request",
                LogLevel.DEBUG
            )
        }
    }

    private fun handleAgentScreenShareRequest(
        operationType: ScreenShareDialogOperationType,
        chatId: Int?
    ) {
        when (operationType) {
            ScreenShareDialogOperationType.CONFIRM -> requestScreenShare(
                isFromRemote = true,
                chatId = chatId
            )

            ScreenShareDialogOperationType.CANCEL -> LoggingUtil.log(
                "User denied agent screen share request",
                LogLevel.DEBUG
            )
        }
    }

    private fun handleActivationRequest(operationType: ScreenShareDialogOperationType) {
        when (operationType) {
            ScreenShareDialogOperationType.CONFIRM -> ScreenShareManager.activateSession()
            ScreenShareDialogOperationType.CANCEL -> stop()
        }
    }

    private fun handleRemoteControlRequest(operationType: ScreenShareDialogOperationType) {
        val enable = operationType == ScreenShareDialogOperationType.CONFIRM
        ScreenShareManager.enableRemoteControl(enable)
    }

    private fun handleFullDeviceRequest(operationType: ScreenShareDialogOperationType) {
        val enable = operationType == ScreenShareDialogOperationType.CONFIRM
        ScreenShareManager.enableFullDeviceSharing(enable)
    }

    private fun handleStopConfirmation(operationType: ScreenShareDialogOperationType) {
        when (operationType) {
            ScreenShareDialogOperationType.CONFIRM -> stop()
            ScreenShareDialogOperationType.CANCEL -> LoggingUtil.log(
                "User cancelled stop screen share",
                LogLevel.DEBUG
            )
        }
    }

    private fun requestScreenShare(isFromRemote: Boolean, chatId: Int?) {
        val chatId = chatId ?: run {
            LoggingUtil.log("Cannot start screen share: Chat ID not available", LogLevel.WARN)
            return
        }

        val initiatedFrom = if (isFromRemote) ScreenShareFrom.AGENT else ScreenShareFrom.END_USER
        screenShareInitiatedFrom = initiatedFrom

        // Send message only for end user initiated requests
        if (!isFromRemote) {
            onSendMessage(ChatMessageEvent.ScreenShareRequestedFromEndUser, null)
        }

        val createScreenShareCallbacks = ScreenShareCallbacks(
            onSessionStateChanged = { state ->
                LoggingUtil.log("Screen share state changed: $state", LogLevel.DEBUG)
            },
            onSessionCreationError = { error ->
                LoggingUtil.log(
                    "Screen share session creation failed: ${error.message}",
                    LogLevel.ERROR
                )
                onSendMessage(ChatMessageEvent.ScreenShareFailed, null)
                stop()
            },
            onSessionActivationRequest = {
                LoggingUtil.log("Screen share activation request received", LogLevel.DEBUG)
                if (screenShareInitiatedFrom == ScreenShareFrom.END_USER) {
                    onShowDialog(ScreenShareDialogType.ACTIVATION_REQUEST)
                } else {
                    ScreenShareManager.activateSession()
                }
            },
            onSessionRemoteControlRequest = {
                LoggingUtil.log("Screen share remote control request received", LogLevel.DEBUG)
                onShowDialog(ScreenShareDialogType.REMOTE_CONTROL_REQUEST)
            },
            onSessionFullDeviceRequest = {
                LoggingUtil.log("Screen share full device request received", LogLevel.DEBUG)
                onShowDialog(ScreenShareDialogType.FULL_DEVICE_REQUEST)
            },
            onSessionDidSucceed = { response ->
                LoggingUtil.log("Screen share session started successfully", LogLevel.DEBUG)
                onSendMessage(ChatMessageEvent.ScreenShareCodeGenerated, response)
            }
        )

        ScreenShareManager.startSession(
            ScreenShareRequest(
                communicationId = chatId,
                communicationType = CommunicationType.Chat,
                initiatedFrom
            ),
            callbacks = createScreenShareCallbacks
        )
    }
}
