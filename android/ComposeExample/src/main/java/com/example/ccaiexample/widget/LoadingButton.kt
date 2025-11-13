package com.example.ccaiexample.widget

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingButton(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = Color.Blue,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Button(
        onClick = {
            onClick?.invoke()
        },
        enabled = enabled && !isLoading,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(text)
        }
    }
}
