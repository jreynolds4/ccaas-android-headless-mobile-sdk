package com.example.xml_example.dialog

import android.content.Context
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType

interface ScreenShareDialogManager {
    fun show(context: Context, dialogType: ScreenShareDialogType)
    fun dismiss()
}
