package com.example.ccaiexample.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogOperationType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType
import com.example.shared.ScreenShareDialogConfigFactory

@Composable
fun ScreenShareDialog(
    dialogType: ScreenShareDialogType? = null,
    onConfirm: (ScreenShareDialogType, ScreenShareDialogOperationType) -> Unit = { _, _ -> },
    onDismiss: () -> Unit = {},
) {
    dialogType?.let { type ->
        val config = ScreenShareDialogConfigFactory.getDialogConfig(
            type,
            onConfirm,
            onDismiss
        )
        CustomDialog(
            title = stringResource(config.titleRes),
            content = stringResource(config.contentRes),
            dismissTitle = stringResource(config.dismissTitleRes),
            confirmTitle = stringResource(config.confirmTitleRes),
            onDismiss = config.onDismiss,
            onConfirm = config.onConfirm
        )
    }
}

@Preview(showBackground = true, name = "User Request Container")
@Composable
fun UserRequestContainerPreview() {
    ScreenShareDialog(
        dialogType = ScreenShareDialogType.USER_SCREEN_SHARE_REQUEST
    )
}

@Preview(showBackground = true, name = "Agent Request Container")
@Composable
fun AgentRequestContainerPreview() {
    ScreenShareDialog(
        dialogType = ScreenShareDialogType.AGENT_SCREEN_SHARE_REQUEST
    )
}

@Preview(showBackground = true, name = "Activation Request Container")
@Composable
fun ActivationRequestContainerPreview() {
    ScreenShareDialog(
        dialogType = ScreenShareDialogType.ACTIVATION_REQUEST
    )
}

@Preview(showBackground = true, name = "Remote Control Request Container")
@Composable
fun RemoteControlRequestContainerPreview() {
    ScreenShareDialog(
        dialogType = ScreenShareDialogType.REMOTE_CONTROL_REQUEST
    )
}

@Preview(showBackground = true, name = "Full Device Request Container")
@Composable
fun FullDeviceRequestContainerPreview() {
    ScreenShareDialog(
        dialogType = ScreenShareDialogType.FULL_DEVICE_REQUEST
    )
}

@Preview(showBackground = true, name = "Stop Confirmation Container")
@Composable
fun StopConfirmationContainerPreview() {
    ScreenShareDialog(
        dialogType = ScreenShareDialogType.STOP_CONFIRMATION
    )
}
