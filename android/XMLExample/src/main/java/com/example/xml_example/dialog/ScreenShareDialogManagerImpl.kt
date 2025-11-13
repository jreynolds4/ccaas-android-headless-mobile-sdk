package com.example.xml_example.dialog

import android.content.Context
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogOperationType
import com.ccaiplatform.ccaikit.interfaces.ScreenShareDialogType
import com.example.shared.ScreenShareDialogConfigFactory

class ScreenShareDialogManagerImpl(
    private val onConfirm: (ScreenShareDialogType, ScreenShareDialogOperationType) -> Unit = { _, _ -> },
    private val onDismiss: () -> Unit = {}
) :
    ScreenShareDialogManager {

    private var currentDialog: CustomDialog? = null

    override fun show(
        context: Context,
        dialogType: ScreenShareDialogType
    ) {
        dismiss()

        val config = ScreenShareDialogConfigFactory.getDialogConfig(
            dialogType, onConfirm, onDismiss
        )
        currentDialog = CustomDialog(
            context = context,
            title = context.getString(config.titleRes),
            content = context.getString(config.contentRes),
            dismissTitle = context.getString(config.dismissTitleRes),
            confirmTitle = context.getString(config.confirmTitleRes),
            onDismiss = config.onDismiss,
            onConfirm = config.onConfirm
        )
        currentDialog?.show()
    }

    override fun dismiss() {
        currentDialog?.dismiss()
        currentDialog = null
    }
}
