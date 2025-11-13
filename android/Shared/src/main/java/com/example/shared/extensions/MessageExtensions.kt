package com.example.shared.extensions

import android.content.Context
import com.ccaiplatform.ccaichat.model.enum.ChatMessageEvent
import com.example.shared.R
import com.example.shared.model.Message

fun Message.displayText(context: Context): String = when {
    event != ChatMessageEvent.None -> {
        when (event) {
            ChatMessageEvent.ScreenShareRequestedFromEndUser -> context.getString(R.string.chat_notification_cobrowse_session_request_sent)
            ChatMessageEvent.ScreenShareStarted -> context.getString(R.string.chat_notification_cobrowse_session_started)
            ChatMessageEvent.ScreenShareEnded -> context.getString(R.string.chat_notification_cobrowse_session_ended)
            ChatMessageEvent.ScreenShareFailed -> context.getString(R.string.chat_notification_cobrowse_session_failed)
            else -> ""
        }
    }
    else -> text ?: ""
}
