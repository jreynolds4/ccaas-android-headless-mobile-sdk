package com.example.ccaiexample.messageitem

import androidx.compose.runtime.Composable
import com.example.shared.model.Message
import com.example.shared.model.MessageType

@Composable
fun MessageItem(message: Message) {
    when (message.type) {
        MessageType.Photo -> ImageMessageItem(message)

        MessageType.Notification -> SystemMessageItem(message)

        else -> {
            TextMessageItem(message)
        }
    }
}
