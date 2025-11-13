package com.example.shared.extensions

import com.ccaiplatform.ccaichat.model.ChatMessage
import com.ccaiplatform.ccaichat.model.enum.ChatMessageEvent

fun List<ChatMessage>.findScreenShareRequest(): Boolean {
    return fold(false) { shouldShow, message ->
        when (message.body.event) {
            ChatMessageEvent.ScreenShareRequestedFromAgent -> true
            ChatMessageEvent.ScreenShareEnded -> false
            else -> shouldShow
        }
    }
}
