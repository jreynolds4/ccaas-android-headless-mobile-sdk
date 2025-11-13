package com.example.ccaiexample.messageitem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ccai.example.compose_example.R
import com.example.ccaiexample.widget.ImageLoader
import com.example.shared.model.Message.User

@Composable
fun MessageAgent(user: User?, modifier: Modifier) {
    if (user == null) {
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        ImageLoader(
            model = user.avatarUrl ?: "",
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(9.dp))
                .padding(2.dp),
            errorResource = R.drawable.ic_agent_sample
        )
        Text(
            text = user.name ?: "",
            fontSize = 14.sp,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        )
    }
}
