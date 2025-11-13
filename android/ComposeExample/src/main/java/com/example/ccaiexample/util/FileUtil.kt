package com.example.ccaiexample.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtil {

    private const val PATH_ROOT = "CCAIExample"
    private const val PATH_PHOTO = "photo"

    fun getTakPhotoFile(context: Context): File? {
        val rootDir = File(context.cacheDir, PATH_ROOT)
        if (!rootDir.exists() && !rootDir.mkdirs()) return null

        val dirPath = File(rootDir, PATH_PHOTO)
        if (!dirPath.exists() && !dirPath.mkdirs()) return null

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(dirPath, "IMG_$timestamp.jpg")
    }
}
