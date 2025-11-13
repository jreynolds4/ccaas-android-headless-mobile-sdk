package com.example.ccaiexample.messageitem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ccaiplatform.ccaichat.model.enum.ChatMessageEvent
import com.example.shared.extensions.displayText
import com.example.shared.model.Message

@Composable
fun SystemMessageItem(message: Message) {
    val context = LocalContext.current
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        val content = message.displayText(context)
        if (content.isNotEmpty()) {
            val text = createRef()
            Text(
                text = content,
                fontSize = TextUnit(value = 13f, type = TextUnitType.Sp),
                modifier = Modifier.constrainAs(text) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top, margin = 8.dp)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                    width = Dimension.percent(0.80f)
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Screen Share Started")
@Composable
fun SystemMessageItemScreenShareStartedPreview() {
    val mockMessage = Message(
        id = "1",
        text = null,
        event = ChatMessageEvent.ScreenShareStarted,
        attachments = emptyList()
    )

    SystemMessageItem(message = mockMessage)
}

@Preview(showBackground = true, name = "Text Message")
@Composable
fun SystemMessageItemTextPreview() {
    val mockMessage = Message(
        id = "2",
        text = "This is a system message",
        event = ChatMessageEvent.None,
        attachments = emptyList()
    )

    SystemMessageItem(message = mockMessage)
}
