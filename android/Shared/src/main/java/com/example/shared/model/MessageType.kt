package com.example.shared.model

enum class MessageType(val value: String) {
    Unknown("unknown"),
    Text("text"),
    Photo("photo"),
    Video("video"),
    Notification("noti"),
    TextTemplate("text_template"),
    Markdown("markdown"),
    MarkdownTemplate("markdown_template"),
    InlineButton("inline_button"),
    StickyButton("sticky_button"),
    Document("document"),
    Image("image"),
    ContentCard("content_card"),
    Form("form"),
    FormComplete("form_complete"),
    ServerMessage("server_message");


    companion object {
        fun fromValue(value: String): MessageType? =
            MessageType.entries.find { it.value == value }
    }
}
