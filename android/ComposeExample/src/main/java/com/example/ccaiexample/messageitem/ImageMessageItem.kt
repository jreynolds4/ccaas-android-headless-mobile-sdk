package com.example.ccaiexample.messageitem

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.ccaiplatform.ccaikit.models.response.communication.Agent
import com.example.ccaiexample.widget.ImageLoader
import com.example.shared.model.Message
import com.example.shared.model.UserRole

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ImageMessageItem(message: Message, agent: Agent? = null) {
    val context = LocalContext.current
    val userRole = message.user.role
    val isEndUser = userRole == UserRole.CURRENT_USER
    val alignment = if (userRole == UserRole.CURRENT_USER) Alignment.End else Alignment.Start
    val maxWidthPercentage = 0.85f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = alignment
    ) {
        if (!isEndUser) {
            MessageAgent(message.user, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        val content = message.text
        if (!content.isNullOrEmpty()) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (isEndUser) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                TextItemContent(
                    content = message.text ?: "",
                    isEndUser = isEndUser,
                    maxWidth = this.maxWidth * maxWidthPercentage
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (isEndUser) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            message.attachments.forEach { attachment ->
                BasicImageItem(
                    imageUrl = attachment.url ?: "",
                    modifier = Modifier
                        .wrapContentHeight()
                        .heightIn(max = 480.dp)
                        .wrapContentWidth()
                        .widthIn(min = 200.dp, max = maxWidth * maxWidthPercentage)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        BasicTimestampItem(message.createdAt)
    }
}

@Composable
fun BasicImageItem(imageUrl: String, modifier: Modifier) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = Modifier
                .size(180.dp, 120.dp)
                .background(
                    color = Color(0xFF72C0C0),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
        }
    } else {
        ImageLoader(
            model = imageUrl,
            modifier = modifier
        )
    }
}
