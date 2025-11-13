package com.example.shared

import android.content.Context
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogConfig
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogOperationType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType

class ScreenShareDialogConfigFactory {
    companion object {
        private fun createDismissCallback(
            dialogType: ScreenShareDialogType,
            onDismiss: () -> Unit,
            onAction: (ScreenShareDialogType, ScreenShareDialogOperationType) -> Unit
        ) = {
            onDismiss()
            onAction(dialogType, ScreenShareDialogOperationType.CANCEL)
        }

        private fun createConfirmCallback(
            dialogType: ScreenShareDialogType,
            onDismiss: () -> Unit,
            onAction: (ScreenShareDialogType, ScreenShareDialogOperationType) -> Unit
        ) = {
            onDismiss()
            onAction(dialogType, ScreenShareDialogOperationType.CONFIRM)
        }

        fun getDialogConfig(
            dialogType: ScreenShareDialogType,
            onAction: (ScreenShareDialogType, ScreenShareDialogOperationType) -> Unit,
            onDismiss: () -> Unit
        ): ScreenShareDialogConfig {
            val dismissCallback = createDismissCallback(dialogType, onDismiss, onAction)
            val confirmCallback = createConfirmCallback(dialogType, onDismiss, onAction)

            when (dialogType) {
                ScreenShareDialogType.USER_SCREEN_SHARE_REQUEST ->
                    return ScreenShareDialogConfig(
                        titleRes = R.string.cobrowse_session_initiate_alert_title,
                        contentRes = R.string.cobrowse_session_initiate_alert_message,
                        dismissTitleRes = R.string.cobrowse_session_initiate_alert_button_no,
                        confirmTitleRes = R.string.cobrowse_session_initiate_alert_button_yes,
                        onDismiss = dismissCallback,
                        onConfirm = confirmCallback
                    )

                ScreenShareDialogType.AGENT_SCREEN_SHARE_REQUEST ->
                    return ScreenShareDialogConfig(
                        titleRes = R.string.cobrowse_session_request_alert_title,
                        contentRes = R.string.cobrowse_session_request_alert_message,
                        dismissTitleRes = R.string.cobrowse_session_request_alert_button_deny,
                        confirmTitleRes = R.string.cobrowse_session_request_alert_button_allow,
                        onDismiss = dismissCallback,
                        onConfirm = confirmCallback
                    )

                ScreenShareDialogType.ACTIVATION_REQUEST ->
                    return ScreenShareDialogConfig(
                        titleRes = R.string.cobrowse_session_request_alert_title,
                        contentRes = R.string.cobrowse_session_request_alert_message,
                        dismissTitleRes = R.string.cobrowse_session_request_alert_button_deny,
                        confirmTitleRes = R.string.cobrowse_session_request_alert_button_allow,
                        onDismiss = dismissCallback,
                        onConfirm = confirmCallback
                    )

                ScreenShareDialogType.REMOTE_CONTROL_REQUEST ->
                    return ScreenShareDialogConfig(
                        titleRes = R.string.cobrowse_remote_access_request_alert_title,
                        contentRes = R.string.cobrowse_remote_access_request_alert_message,
                        dismissTitleRes = R.string.cobrowse_remote_access_request_alert_button_deny,
                        confirmTitleRes = R.string.cobrowse_remote_access_request_alert_button_allow,
                        onDismiss = dismissCallback,
                        onConfirm = confirmCallback
                    )

                ScreenShareDialogType.FULL_DEVICE_REQUEST ->
                    return ScreenShareDialogConfig(
                        titleRes = R.string.cobrowse_screen_share_request_alert_title,
                        contentRes = R.string.cobrowse_screen_share_request_alert_message,
                        dismissTitleRes = R.string.cobrowse_screen_share_request_alert_button_deny,
                        confirmTitleRes = R.string.cobrowse_screen_share_request_alert_button_allow,
                        onDismiss = dismissCallback,
                        onConfirm = confirmCallback
                    )

                ScreenShareDialogType.STOP_CONFIRMATION ->
                    return ScreenShareDialogConfig(
                        titleRes = R.string.cobrowse_session_end_alert_title,
                        contentRes = R.string.cobrowse_session_end_alert_message,
                        dismissTitleRes = R.string.cobrowse_session_end_alert_button_no,
                        confirmTitleRes = R.string.cobrowse_session_end_alert_button_yes,
                        onDismiss = dismissCallback,
                        onConfirm = confirmCallback
                    )
            }
        }
    }
}
