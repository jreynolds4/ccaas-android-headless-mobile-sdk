package com.example.xml_example.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import co.ccai.example.xml_example.R
import com.ccaiplatform.ccaikit.interfaces.ScreenShareSessionState
import com.ccaiplatform.ccaikit.interfaces.isActive
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChatMenuBottomSheet : BottomSheetDialogFragment() {

    companion object {

        /**
         * The ratio of the screen height that the bottom sheet should occupy.
         * Set to 0.8 to ensure the bottom sheet covers 80% of the screen height,
         * providing enough space for content while leaving part of the background visible.
         */
        private const val BOTTOM_SHEET_HEIGHT_RATIO = 0.8
    }

    private var onDismissCallback: (() -> Unit)? = null
    private var onExitChatCallback: (() -> Unit)? = null
    private var toggleScreenShareCallback: (() -> Unit)? = null
    private var currentScreenShareSessionState: ScreenShareSessionState? = null
    private var currentIsScreenShareEnabled: Boolean = false

    private lateinit var tvClose: TextView
    private lateinit var ivScreenShareIcon: ImageView
    private lateinit var tvScreenShare: TextView
    private lateinit var llScreenShare: LinearLayout
    private lateinit var btnExitChat: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chat_menu_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvClose = view.findViewById(R.id.tv_close)
        llScreenShare = view.findViewById(R.id.ll_screen_share)
        ivScreenShareIcon = view.findViewById(R.id.iv_screen_share_icon)
        tvScreenShare = view.findViewById(R.id.tv_screen_share)
        btnExitChat = view.findViewById(R.id.btn_exit_chat)

        setupViews()
        updateUI()
    }

    private fun updateUI() {
        currentScreenShareSessionState?.let { state ->
            updateScreenShareUI(state.isActive())
        }
        updateScreenShareVisibility(currentIsScreenShareEnabled)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setOnShowListener { dialog ->
                val bottomSheetDialog = dialog as BottomSheetDialog
                val bottomSheet =
                    bottomSheetDialog.findViewById<View>(
                        com.google.android.material.R.id.design_bottom_sheet
                    )
                bottomSheet?.let {
                    it.layoutParams.height =
                        (resources.displayMetrics.heightPixels * BOTTOM_SHEET_HEIGHT_RATIO).toInt()
                    it.requestLayout()

                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    private fun setupViews() {
        // Close button
        tvClose.setOnClickListener {
            onDismissCallback?.invoke()
            dismiss()
        }

        // Screen share setup
        llScreenShare.setOnClickListener {
            toggleScreenShareCallback?.invoke()
        }

        // Exit chat button
        btnExitChat.setOnClickListener {
            onExitChatCallback?.invoke()
            dismiss()
        }
    }

    private fun updateScreenShareVisibility(isEnabled: Boolean) {
        llScreenShare.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    private fun updateScreenShareUI(isActive: Boolean) {
        val context = requireContext()
        if (isActive) {
            ivScreenShareIcon.setColorFilter(ContextCompat.getColor(context, R.color.screen_share_button_active))
            tvScreenShare.setTextColor(ContextCompat.getColor(context, R.color.screen_share_button_active))
            tvScreenShare.text = getString(com.example.shared.R.string.cobrowse_session_end_button_title)
        } else {
            ivScreenShareIcon.setColorFilter(ContextCompat.getColor(context, R.color.screen_share_button_inactive))
            tvScreenShare.setTextColor(ContextCompat.getColor(context, R.color.screen_share_button_inactive))
            tvScreenShare.text = getString(com.example.shared.R.string.cobrowse_session_initiate_button_title)
        }
    }

    fun setCallbacks(
        onDismiss: () -> Unit,
        onExitChat: () -> Unit,
        currentScreenShareSessionState: ScreenShareSessionState?,
        currentIsScreenShareEnabled: Boolean,
        toggleScreenShare: () -> Unit
    ) {
        this.onDismissCallback = onDismiss
        this.onExitChatCallback = onExitChat
        this.currentScreenShareSessionState = currentScreenShareSessionState
        this.currentIsScreenShareEnabled = currentIsScreenShareEnabled
        this.toggleScreenShareCallback = toggleScreenShare
    }

    fun updateScreenShareState(state: ScreenShareSessionState?) {
        this.currentScreenShareSessionState = state
        updateScreenShareUI(state?.isActive() ?: false)
    }

    fun updateScreenShareEnabled(isEnabled: Boolean) {
        this.currentIsScreenShareEnabled = isEnabled
        updateScreenShareVisibility(isEnabled)
    }
}
