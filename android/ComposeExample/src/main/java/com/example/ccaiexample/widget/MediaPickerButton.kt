package com.example.ccaiexample.widget

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import co.ccai.example.compose_example.R
import com.example.ccaiexample.util.MediaUtil
import kotlinx.coroutines.launch
import kotlin.io.readBytes
import kotlin.io.use

@Composable
fun MediaPickerButton(
    onImageUpload: suspend (ByteArray, Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pickPhoto = MediaUtil.pickPhoto { uri ->
        isLoading = true
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            scope.launch { onImageUpload(bytes, uri) }
        }
        isLoading = false
    }
    val takePhoto = MediaUtil.takePhoto { uri ->
        isLoading = true
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bytes = inputStream.readBytes()
            scope.launch { onImageUpload(bytes, uri) }
        }
        isLoading = false
    }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showMenu = true },
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = context.getString(R.string.attach_media)
            )
            if (isLoading) {
                CircularProgressIndicator()
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(context.getString(R.string.take_photo)) },
                onClick = {
                    showMenu = false
                    takePhoto.invoke()
                }
            )
            DropdownMenuItem(
                text = { Text(context.getString(R.string.select_photo)) },
                onClick = {
                    showMenu = false
                    pickPhoto.invoke()
                }
            )
        }
    }
}
