package com.example.xml_example.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ccaiplatform.ccaichat.model.ChatResponse
import com.ccaiplatform.ccaichat.model.enum.ChatProviderState
import co.ccai.example.xml_example.R
import com.ccaiplatform.ccaikit.interfaces.ScreenShareSessionState
import com.ccaiplatform.ccaikit.interfaces.isActive
import com.ccaiplatform.ccaikit.models.logger.LogLevel
import com.ccaiplatform.ccaikit.util.PermissionUtil
import com.ccaiplatform.ccaikit.util.logging.LoggingUtil
import com.example.shared.util.SystemUtil
import com.example.xml_example.adapter.MessageAdapter
import com.example.xml_example.dialog.CustomDialog
import com.example.xml_example.dialog.LoadingDialog
import com.example.xml_example.dialog.ScreenShareDialogManager
import com.example.xml_example.dialog.ScreenShareDialogManagerImpl
import com.example.xml_example.util.FileUtil
import com.example.xml_example.util.StatusBarController
import com.example.xml_example.viewmodel.ChatViewModel
import com.example.xml_example.widget.ButtonType
import com.example.xml_example.widget.MediaPickerButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ChatFragment : Fragment() {
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var loadingDialog: LoadingDialog? = null
    private var endingDialog: LoadingDialog? = null
    private var endChatDialog: CustomDialog? = null
    private var chatMenuBottomSheet: ChatMenuBottomSheet? = null
    private var pendingPhotoUri: Uri? = null
    private var notificationPermissionDialog: CustomDialog? = null
    private var notificationOffDialog: CustomDialog? = null

    private val screenShareDialogManager: ScreenShareDialogManager by lazy {
        ScreenShareDialogManagerImpl(
            onConfirm = viewModel::handleScreenShareDialogConfirm,
            onDismiss = viewModel::handleScreenShareDialogDismiss
        )
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                pendingPhotoUri?.let { handleImageSelection(it) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = context ?: return
        if (!PermissionUtil.isPermissionsForNotificationsGranted(context) &&
            !PermissionUtil.isSetNotificationPermissionOff(context)
        ) {
            showNotificationPermissionDialog(context)
        }
        arguments?.let {
            val menuId = it.getInt("menuId", 0)
            val chat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable("chat", ChatResponse::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable("chat") as? ChatResponse
            }

            viewModel.updateMenuId(menuId)
            if (chat != null) {
                viewModel.resumeChat(chat)
            } else {
                viewModel.startChat()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        val editTextMessage = view.findViewById<EditText>(R.id.editTextMessage)
        val buttonSend = view.findViewById<View>(R.id.sendButton)
        val buttonMediaPicker = view.findViewById<MediaPickerButton>(R.id.mediaPickerButton)
        val buttonBack = view.findViewById<ImageButton>(R.id.buttonBack)
        val buttonMenu = view.findViewById<ImageButton>(R.id.buttonMenu)

        messageAdapter = MessageAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
        }

        buttonSend.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendTextMessage(message)
                editTextMessage.text.clear()
                hideKeyboard(editTextMessage)
            }
        }
        buttonMediaPicker.setOnButtonClickListener(object : MediaPickerButton.OnButtonClickListener {
            override fun onButtonClick(buttonType: ButtonType) {
                when (buttonType) {
                    ButtonType.PHOTO -> {
                        val uri = createImageUri()
                        if (uri != null) {
                            pendingPhotoUri = uri
                            takePhotoLauncher.launch(uri)
                        } else {
                            Log.e("ChatFragment", "Failed to create image URI")
                        }
                    }

                    ButtonType.IMAGE -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                        }
                        pickImage.launch(intent)
                    }
                }
            }
        })

        buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        buttonMenu.setOnClickListener {
            showChatMenuBottomSheet()
        }

        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                    error?.let {
                        showErrorDialog(it)
                    }
                }

                viewModel.messages.observe(viewLifecycleOwner) { messages ->
                    val isRefreshing = viewModel.isRefreshing.value ?: false
                    messageAdapter.submitList(messages) {
                        if (messages.isNotEmpty() && !isRefreshing) {
                            scrollToLastMessage()
                        }
                    }
                }

                viewModel.state.observe(viewLifecycleOwner) { state ->
                    updateInputVisibility(state)
                }

                viewModel.isSending.observe(viewLifecycleOwner) { isSending ->
                    updateSendButtonState(isSending)
                }

                viewModel.showLoadingDialog.observe(viewLifecycleOwner) { pair ->
                    if (pair.first) {
                        showLoadingDialog(pair.second)
                    } else {
                        hideLoadingDialog()
                    }
                }

                viewModel.currentScreenShareDialogType.observe(viewLifecycleOwner) { dialogType ->
                    dialogType?.let { type ->
                        screenShareDialogManager.show(requireContext(), type)
                    } ?: run {
                        screenShareDialogManager.dismiss()
                    }
                }

                // Observe screen share states to update bottom sheet
                viewModel.screenShareSessionState.observe(viewLifecycleOwner) { state ->
                    chatMenuBottomSheet?.updateScreenShareState(state)
                    onScreenShareSessionStateUpdate(state)
                }

                viewModel.isScreenShareEnabled.observe(viewLifecycleOwner) { isEnabled ->
                    chatMenuBottomSheet?.updateScreenShareEnabled(isEnabled)
                }
            }
        }
    }

    private fun updateInputVisibility(state: ChatProviderState) {
        val isConnected = state == ChatProviderState.Connected
        val chatStatus = viewModel.chatStatus.value
        val isStatusValid = chatStatus != null && chatStatus.isInProgress() && !chatStatus.isDismissed()
        val showInput = isConnected && isStatusValid

        view?.findViewById<View>(R.id.layoutInput)?.visibility =
            if (showInput) View.VISIBLE else View.GONE
    }

    private fun updateSendButtonState(isSending: Boolean) {
        view?.findViewById<TextView>(R.id.sendButton)?.apply {
            isEnabled = !isSending
            text = getString(R.string.send)
        }
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
            if (isSending) View.VISIBLE else View.GONE
    }

    private fun scrollToLastMessage() {
        recyclerView.post {
            val lastPosition = messageAdapter.itemCount - 1
            if (lastPosition >= 0) {
                recyclerView.smoothScrollToPosition(lastPosition)
            }
        }
    }

    private fun showChatMenuBottomSheet() {
        chatMenuBottomSheet = ChatMenuBottomSheet()
        chatMenuBottomSheet?.setCallbacks(
            onDismiss = {
                chatMenuBottomSheet?.dismiss()
            },
            onExitChat = {
                showEndChatDialog()
            },
            currentScreenShareSessionState = viewModel.screenShareSessionState.value,
            currentIsScreenShareEnabled = viewModel.isScreenShareEnabled.value ?: false,
            toggleScreenShare = {
                viewModel.toggleScreenShare()
            }
        )
        chatMenuBottomSheet?.show(parentFragmentManager, "ChatMenuBottomSheet")
    }

    private fun onScreenShareSessionStateUpdate(state: ScreenShareSessionState) {
        val color = if (state.isActive()) {
            ContextCompat.getColor(requireContext(), R.color.screen_share_active)
        } else {
            ContextCompat.getColor(requireContext(), R.color.screen_share_inactive)
        }
        (activity as? StatusBarController)?.updateStatusBarColor(color)
    }

    private fun showEndChatDialog() {
        endChatDialog = CustomDialog(
            context = requireContext(),
            content = getString(R.string.ask_end_chat),
            onDismiss = { },
            onConfirm = {
                viewModel.endChat {
                    parentFragmentManager.popBackStack()
                }
            }
        )
        endChatDialog?.show()
    }

    private fun showErrorDialog(message: String) {
        CustomDialog(
            context = requireContext(),
            content = message,
            showDismissButton = false,
            onConfirm = { }
        ).show()
    }

    private fun showLoadingDialog(message: String) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(requireContext(), message)
        }
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss()
        endingDialog?.dismiss()
        endChatDialog?.dismiss()
        notificationPermissionDialog?.dismiss()
        notificationOffDialog?.dismiss()
    }

    private fun handleImageSelection(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }

                if (bytes != null) {
                    withContext(Dispatchers.Main) {
                        viewModel.sendPhotoMessage(bytes, uri)
                    }
                }
            } catch (e: IOException) {
                Log.e("MediaPickerButton", "Error reading image: ${e.message}")
            }
        }
    }

    private fun createImageUri(): Uri? {
        val photoFile = context?.let { FileUtil.getTakPhotoFile(it) }
        return photoFile?.let {
            FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".fileprovider",
                it
            )
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshMessages()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
                    swipeRefreshLayout.isRefreshing = isRefreshing
                }
            }
        }
    }
    
    private fun showNotificationPermissionDialog(context: Context) {
        notificationPermissionDialog = CustomDialog(
            context = context,
            content = getString(R.string.dialog_notification_permission_content),
            dismissTitle = getString(R.string.btn_dont_allow),
            confirmTitle = getString(R.string.btn_allow),
            onDismiss = {
                PermissionUtil.setNotificationPermissionOff(context)
                showNotificationOffDialog(context)
            },
            onConfirm = {
                context.findActivity()?.let { activity ->
                    PermissionUtil.requestPermissionsForNotifications(activity)
                } ?: run {
                    LoggingUtil.log("Context is not an Activity", LogLevel.ERROR)
                }
            }
        )
        notificationPermissionDialog?.show()
    }
    
    private fun showNotificationOffDialog(context: Context) {
        notificationOffDialog = CustomDialog(
            context = context,
            title = getString(R.string.dialog_notification_off_title),
            content = getString(R.string.dialog_notification_off_content),
            dismissTitle = getString(R.string.btn_no_thanks),
            confirmTitle = getString(R.string.btn_go_settings),
            onConfirm = {
                SystemUtil.openNotificationSettings(context, R.string.open_settings_failed)
            }
        )
        notificationOffDialog?.show()
    }
}

fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> null
    }
}
