package com.example.shared

import com.ccaiplatform.ccaichat.model.enum.ChatStatus
import com.ccaiplatform.ccaikit.ScreenShareManager

object ScreenShareEligibility {
    fun isEnabled(chatStatus: ChatStatus?, supportsScreenShare: Boolean?): Boolean {
        return chatStatus?.isAssigned() == true &&
            supportsScreenShare == true &&
            ScreenShareManager.isAvailable()
    }
}
