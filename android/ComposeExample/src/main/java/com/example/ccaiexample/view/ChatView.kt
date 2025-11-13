package com.example.ccaiexample.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ccaiplatform.ccaichat.model.ChatResponse
import com.ccaiplatform.ccaichat.model.enum.ChatProviderState
import co.ccai.example.compose_example.R
import com.ccaiplatform.ccaikit.ScreenShareManager
import com.ccaiplatform.ccaikit.interfaces.ScreenShareSessionState
import com.ccaiplatform.ccaikit.interfaces.isActive
import com.ccaiplatform.ccaikit.models.logger.LogLevel
import com.ccaiplatform.ccaikit.util.ApplicationUtil
import com.ccaiplatform.ccaikit.util.PermissionUtil
import com.ccaiplatform.ccaikit.util.logging.LoggingUtil
import com.example.ccaiexample.dialog.LoadingDialog
import com.example.ccaiexample.dialog.CustomDialog
import com.example.ccaiexample.dialog.ScreenShareDialog
import com.example.ccaiexample.viewmodel.ChatViewModel
import com.example.shared.util.SystemUtil
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(
    navController: NavHostController,
) {
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val menuId = savedStateHandle?.get<Int>("menuId")
    val previousChat = savedStateHandle?.get<ChatResponse>("chat")
    if (menuId == null && previousChat == null) return

    val context = LocalContext.current
    val chatViewModel = viewModel<ChatViewModel>()

    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showNotificationOffDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!PermissionUtil.isPermissionsForNotificationsGranted(context) &&
            !PermissionUtil.isSetNotificationPermissionOff(context)
        ) {
            showNotificationPermissionDialog = true
        }
    }

    if (previousChat == null) {
        LaunchedEffect(menuId) {
            menuId?.let {
                chatViewModel.updateMenuId(menuId)
                chatViewModel.startChat()
            }
        }
    } else {
        LaunchedEffect(previousChat) {
            chatViewModel.updateMenuId(previousChat.menus?.firstOrNull()?.id ?: -1)
            chatViewModel.resumeChat(previousChat)
        }
    }

    val msgError = chatViewModel.errorMessage
    LaunchedEffect(msgError) {
        msgError?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    val messages = chatViewModel.messages
    val state = chatViewModel.state
    val showEndingDialog = chatViewModel.showEndingDialog
    val chat = chatViewModel.chat
    val chatStatus = chatViewModel.chatStatus
    val isSending = chatViewModel.isSending
    val isRefreshing = chatViewModel.isRefreshing
    val isScreenShareEnabled = chatViewModel.isScreenShareEnabled

    val keyboardController = LocalSoftwareKeyboardController.current
    var showEndingChatDialog by remember { mutableStateOf(false) }
    var showChatMenuBottomSheet by remember { mutableStateOf(false) }
    var screenShareSessionState by remember { mutableStateOf(ScreenShareSessionState.INACTIVE) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = chat?.currentAgent?.name?.let { agentName ->
                            stringResource(R.string.chat_title_connected, agentName)
                        } ?: stringResource(R.string.chat),
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier
                            .semantics {
                                this.contentDescription = "chat menu button"
                            },
                        onClick = {
                            keyboardController?.hide()
                            showChatMenuBottomSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.menu)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->

        ChatContentView(
            messages = messages,
            state = state,
            isSending = isSending,
            isRefreshing = isRefreshing,
            chatStatus = chatStatus,
            onUploadPhoto = { data, uri ->
                chatViewModel.sendPhotoMessage(data, uri)
            },
            innerPadding = innerPadding,
            onActionButtonClick = { text ->
                chatViewModel.sendTextMessage(text)
            },
            refreshMessages = {
                chatViewModel.refreshMessages()
            },
        )

        LoadingDialog(
            isShowing = state == ChatProviderState.Connecting,
            message = stringResource(R.string.connecting),
            onDismiss = {
                navController.popBackStack()
            }
        )

        LoadingDialog(
            isShowing = showEndingDialog,
            message = stringResource(R.string.ending_chat),
        )

        // Show notification permission dialog if needed
        if (showNotificationPermissionDialog) {
            CustomDialog(
                title = context.getString(
                    R.string.dialog_notification_permission_title,
                    ApplicationUtil.getApplicationName(context)
                ),
                content = stringResource(R.string.dialog_notification_permission_content),
                dismissTitle = stringResource(R.string.btn_dont_allow),
                confirmTitle = stringResource(R.string.btn_allow),
                onDismiss = {
                    PermissionUtil.setNotificationPermissionOff(context)
                    showNotificationPermissionDialog = false
                    showNotificationOffDialog = true
                },
                onConfirm = {
                    showNotificationPermissionDialog = false
                    context.findActivity()?.let { activity ->
                        PermissionUtil.requestPermissionsForNotifications(activity)
                    } ?: run {
                        LoggingUtil.log("Context is not an Activity", LogLevel.ERROR)
                    }
                }
            )
        }

        if (showNotificationOffDialog) {
            CustomDialog(
                title = stringResource(R.string.dialog_notification_off_title),
                content = stringResource(R.string.dialog_notification_off_content),
                dismissTitle = stringResource(R.string.btn_no_thanks),
                confirmTitle = stringResource(R.string.btn_go_settings),
                onDismiss = { showNotificationOffDialog = false },
                onConfirm = {
                    showNotificationOffDialog = false
                    SystemUtil.openNotificationSettings(context, R.string.open_settings_failed)
                }
            )
        }

        if (showEndingChatDialog) {
            CustomDialog(
                content = stringResource(R.string.ask_end_chat),
                onDismiss = { showEndingChatDialog = false },
                onConfirm = {
                    showEndingChatDialog = false
                    chatViewModel.endChat { navController.popBackStack() }
                })
        }

        if (showChatMenuBottomSheet) {
            ChatMenuBottomSheet(
                onDismiss = { showChatMenuBottomSheet = false },
                onExitChat = {
                    showChatMenuBottomSheet = false
                    showEndingChatDialog = true
                },
                isScreenShareActive = screenShareSessionState.isActive(),
                isScreenShareEnabled = isScreenShareEnabled,
                toggleScreenShare = {
                    chatViewModel.toggleScreenShare()
                }
            )
        }

        ChatScreenShareSection(
            onScreenShareSessionStateUpdate = { newState ->
                screenShareSessionState = newState
            },
            screenShareDialog = {
                ScreenShareDialog(
                    dialogType = chatViewModel.currentScreenShareDialogType,
                    onConfirm = chatViewModel::handleScreenShareDialogConfirm,
                    onDismiss = chatViewModel::handleScreenShareDialogDismiss,
                )
            }
        )
    }
}

@Composable
fun ChatScreenShareSection(
    onScreenShareSessionStateUpdate: (ScreenShareSessionState) -> Unit,
    screenShareDialog: (@Composable () -> Unit)? = null,
) {
    val systemUiController = rememberSystemUiController()
    
    val screenShareActiveColor = colorResource(id = R.color.screen_share_active)
    val screenShareInactiveColor = colorResource(id = R.color.screen_share_inactive)

    LaunchedEffect(Unit) {
        ScreenShareManager.getSessionState()?.let { onScreenShareSessionStateUpdate(it) }
    }

    DisposableEffect(Unit) {
        val listener = { state: ScreenShareSessionState ->
            onScreenShareSessionStateUpdate(state)
            val statusBarColor = if (state.isActive()) {
                screenShareActiveColor
            } else {
                screenShareInactiveColor
            }
            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = true
            )
        }
        ScreenShareManager.addStateChangeListener(listener)
        onDispose {
            ScreenShareManager.removeStateChangeListener(listener)
        }
    }

    screenShareDialog?.invoke()
}

fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> null
    }
}
