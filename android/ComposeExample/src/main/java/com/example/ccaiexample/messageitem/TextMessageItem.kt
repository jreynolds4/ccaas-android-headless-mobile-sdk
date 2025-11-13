package com.example.ccaiexample.messageitem

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.example.shared.extensions.displayText
import com.example.shared.model.Message
import com.example.shared.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TextMessageItem(message: Message) {
    val userRole = message.user.role
    val isEndUser = userRole == UserRole.CURRENT_USER
    val alignment = if (userRole == UserRole.CURRENT_USER) Alignment.End else Alignment.Start
    val maxWidthPercentage = 0.85f
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = alignment
    ) {

        if (!isEndUser) {
            MessageAgent(message.user, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (isEndUser) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            TextItemContent(
                content = message.displayText(context),
                isEndUser = isEndUser,
                maxWidth = this.maxWidth * maxWidthPercentage
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        BasicTimestampItem(message.createdAt)
    }
}

@Composable
fun BasicTimestampItem(timestamp: Date) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent),
        border = null
    ) {
        Text(
            text = SimpleDateFormat("HH:mm a", Locale.US).format(timestamp),
            fontSize = TextUnit(value = 11f, type = TextUnitType.Sp),
        )
    }
}

@Preview(showBackground = true, name = "End User Message")
@Composable
fun TextMessageItemEndUserPreview() {
    val mockUser = Message.User(
        id = "1",
        name = "Current User",
        role = UserRole.CURRENT_USER
    )
    val mockMessage = Message(
        id = "1",
        text = "Hello, this is a message from the current user",
        user = mockUser,
        createdAt = Date(),
        attachments = emptyList()
    )

    TextMessageItem(message = mockMessage)
}

@Preview(showBackground = true, name = "System Message")
@Composable
fun TextMessageItemSystemPreview() {
    val mockUser = Message.User(
        id = "2",
        name = "System",
        role = UserRole.SYSTEM
    )
    val mockMessage = Message(
        id = "2",
        text = "Hi there! How can I help you today?",
        user = mockUser,
        createdAt = Date(),
        attachments = emptyList()
    )

    TextMessageItem(message = mockMessage)
}

@Preview(showBackground = true)
@Composable
fun BasicTimestampItemPreview() {
    BasicTimestampItem(timestamp = Date())
}
