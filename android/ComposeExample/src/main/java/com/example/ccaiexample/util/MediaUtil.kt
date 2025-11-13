package com.example.ccaiexample.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlin.let

object MediaUtil {

    @Composable
    fun pickPhoto(callback: (uri: Uri) -> Unit): (() -> Unit) {
        val context = LocalContext.current

        val isPhotoPickerAvailable = remember {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
        }
        val modernPhotoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let { callback.invoke(uri) }
        }
        val legacyPhotoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    callback.invoke(uri)
                }
            }
        }
        return {
            if (isPhotoPickerAvailable) {
                modernPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                legacyPhotoPickerLauncher.launch(intent)
            }
        }
    }

    @Composable
    fun takePhoto(callback: (uri: Uri) -> Unit): (() -> Unit) {
        val context = LocalContext.current
        var photoUri by remember { mutableStateOf<Uri?>(null) }
        val takePictureLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            val uri = photoUri ?: return@rememberLauncherForActivityResult
            if (success) {
                callback.invoke(uri)
            }
        }
        val createImageUri = {
            val photosFile = FileUtil.getTakPhotoFile(context) ?: throw IllegalStateException("Failed to create image URI")
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", photosFile)
            uri
        }
        return {
            photoUri = createImageUri()
            takePictureLauncher.launch(photoUri!!)
        }
    }
}
