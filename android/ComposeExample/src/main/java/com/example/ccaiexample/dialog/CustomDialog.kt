package com.example.ccaiexample.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import co.ccai.example.compose_example.R
import kotlin.text.isNullOrEmpty

@Composable
fun CustomDialog(
    title: String? = null,
    content: String,
    dismissTitle: String? = null,
    confirmTitle: String? = null,
    showDismissButton: Boolean = true,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit
) {
    AlertDialog(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.95f)
            .wrapContentHeight(),
        onDismissRequest = onDismiss,
        title = {
            if (!title.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        dismissButton = if (showDismissButton) {
            {
                TextButton(onClick = onDismiss) {
                    Text(text = if (dismissTitle.isNullOrEmpty()) stringResource(R.string.no) else dismissTitle)
                }
            }
        } else null,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = if (confirmTitle.isNullOrEmpty()) stringResource(R.string.yes) else confirmTitle, color = Color.Red)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CustomDialogPreview() {
    CustomDialog(
        content = "This is a custom dialog preview.",
        dismissTitle = "Cancel",
        confirmTitle = "Confirm",
        onDismiss = {},
        onConfirm = {}
    )
}
