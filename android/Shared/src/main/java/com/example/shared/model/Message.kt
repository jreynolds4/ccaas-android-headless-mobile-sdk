package com.example.shared.model

import com.ccaiplatform.ccaichat.model.enum.ChatMessageEvent
import com.ccaiplatform.ccaichat.model.enum.OutgoingMessageContent
import com.example.shared.model.Message.Attachment
import com.example.shared.model.Message.User
import java.util.Date

data class Message(
    val id: String? = null,
    val text: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val user: User = User(),
    val type: MessageType? = null,
    val createdAt: Date = Date(),
    val event: ChatMessageEvent = ChatMessageEvent.None
) {
    data class Attachment(
        val url: String? = null,
        val type: String,
    )

    data class User(
        val id: String? = null,
        val name: String? = null,
        val role: UserRole = UserRole.CURRENT_USER,
        val avatarUrl: String? = null,
    )
}

fun OutgoingMessageContent.convertToDisplayMessageModel(user: User): List<Message> {
    return when (this) {
        is OutgoingMessageContent.Text -> {
            val messageToSend = Message(
                text = content,
                user = user,
                type = MessageType.Text,
                createdAt = Date(),
            )
            listOf(messageToSend)
        }

        is OutgoingMessageContent.Photos -> {
            this.uris.map { uri ->
                Message(
                    user = user,
                    type = MessageType.Photo,
                    attachments = listOf(Attachment(url = uri.toString(), type = MessageType.Photo.value)),
                    createdAt = Date(),
                )
            }
        }

        is OutgoingMessageContent.FormComplete -> {
            val messageToSend = Message(
                text = "",
                user = user,
                type = MessageType.FormComplete,
                createdAt = Date(),
            )
            listOf(messageToSend)
        }

        is OutgoingMessageContent.ScreenShare -> {
            val messageToSend = Message(
                event = ChatMessageEvent.fromValue(event),
                user = user,
                type = MessageType.Notification
            )
            listOf(messageToSend)
        }
    }
}
