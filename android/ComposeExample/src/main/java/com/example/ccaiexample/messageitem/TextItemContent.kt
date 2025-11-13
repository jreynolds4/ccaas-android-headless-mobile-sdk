package com.example.ccaiexample.messageitem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@Composable
fun TextItemContent(content: String, isEndUser: Boolean, maxWidth: Dp) {
    Card(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .widthIn(max = maxWidth),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors()
            .copy(
                containerColor = if (isEndUser) Color.LightGray else Color.Transparent
            ),
        border = if (isEndUser) null else BorderStroke(1.dp, Color(82, 150, 213))

    ) {
        Text(
            text = content,
            fontSize = TextUnit(value = 14f, type = TextUnitType.Sp),
            modifier = Modifier.padding(8.dp),
        )
    }
}
