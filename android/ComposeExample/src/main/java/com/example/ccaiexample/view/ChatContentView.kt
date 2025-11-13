package com.example.ccaiexample.view

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ccaiplatform.ccaichat.model.enum.ChatProviderState
import com.ccaiplatform.ccaichat.model.enum.ChatStatus
import co.ccai.example.compose_example.R
import com.example.ccaiexample.messageitem.MessageItem
import com.example.ccaiexample.widget.LoadingButton
import com.example.ccaiexample.widget.MediaPickerButton
import com.example.shared.model.Message
import kotlinx.coroutines.flow.first
import kotlin.collections.isNotEmpty

typealias OnActionButtonClick = ((text: String) -> Unit)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatContentView(
    messages: List<Message>,
    state: ChatProviderState,
    isSending: Boolean = false,
    isRefreshing: Boolean = false,
    chatStatus: ChatStatus?,
    onUploadPhoto: (ByteArray, Uri) -> Unit,
    onActionButtonClick: OnActionButtonClick? = null,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    refreshMessages: () -> Unit,
) {
    val listState = rememberLazyListState()
    var bottomStickyButtonsPadding by remember { mutableIntStateOf(0) }
    var inputHeightPx by remember { mutableIntStateOf(0) }
    var shouldScrollToBottom by remember { mutableStateOf(true) }

    val keyboardHeight by keyboardHeightAsState()

    LaunchedEffect(messages.size, isRefreshing) {
        if (messages.isNotEmpty() && !isRefreshing && shouldScrollToBottom) {
            snapshotFlow { listState.layoutInfo }
                .first { it.totalItemsCount > 0 }

            val lastIndex = (messages.size - 1).coerceAtLeast(0)
            listState.animateScrollToItem(lastIndex)
        }
        shouldScrollToBottom = !isRefreshing
    }

    LaunchedEffect(keyboardHeight) {
        if (keyboardHeight > 0 && messages.isNotEmpty()) {
            val lastIndex = (messages.size - 1).coerceAtLeast(0)
            val totalOffset = keyboardHeight
            listState.scrollToItem(lastIndex, scrollOffset = totalOffset)
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if (!isRefreshing) {
                refreshMessages()
            }
        }
    )

    Box(Modifier.pullRefresh(state = pullRefreshState)) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .imePadding()
                .navigationBarsPadding()
        ) {

            val (topMessages, bottomInput, bottomButtons) = createRefs()

            LazyColumn(
                contentPadding = PaddingValues(bottom = bottomStickyButtonsPadding.dp),
                modifier = Modifier
                    .padding(innerPadding)
                    .constrainAs(topMessages) {
                        start.linkTo(parent.start, margin = 12.dp)
                        top.linkTo(parent.top, margin = 8.dp)
                        end.linkTo(parent.end, margin = 12.dp)
                        bottom.linkTo(bottomInput.top, margin = 4.dp)
                        height = Dimension.fillToConstraints
                        verticalBias = 0f
                    },
                state = listState
            ) {
                items(messages) { message ->
                    MessageItem(message)
                }
            }

            InputContent(
                state = state,
                chatStatus = chatStatus,
                isSending = isSending,
                onUploadPhoto = onUploadPhoto,
                modifier = Modifier.constrainAs(bottomInput) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                    verticalBias = 1f
                },
                onInputHeightChanged = { height ->
                    inputHeightPx = height
                },
                onActionButtonClick = onActionButtonClick
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
private fun InputContent(
    modifier: Modifier,
    state: ChatProviderState,
    isSending: Boolean = false,
    chatStatus: ChatStatus?,
    onActionButtonClick: OnActionButtonClick? = null,
    onUploadPhoto: (ByteArray, Uri) -> Unit,
    onInputHeightChanged: (height: Int) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .padding(bottom = 10.dp),
    ) {
        val isStateValid = state == ChatProviderState.Connected
        val isStatusValid =
            chatStatus != null && chatStatus.isInProgress() && !chatStatus.isDismissed()
        val showInput = isStateValid && isStatusValid
        if (showInput.not()) return@Row

        MediaPickerButton(
            modifier = Modifier,
            onImageUpload = { data, uri ->
                onUploadPhoto(data, uri)
            }
        )

        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    onInputHeightChanged(coordinates.size.height)
                },
            value = inputText,
            onValueChange = {
                inputText = it
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        )

        Spacer(modifier = Modifier.width(8.dp))
        LoadingButton(
            modifier = Modifier.wrapContentWidth(),
            text = stringResource(R.string.send),
            isLoading = isSending,
            onClick = {
                onActionButtonClick?.invoke(inputText)
                focusManager.clearFocus()
                inputText = ""
            }
        )
        Spacer(modifier = Modifier.width(8.dp))

    }
}

@Composable
private fun keyboardHeightAsState(): MutableState<Int> {
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current

    val height = remember { mutableIntStateOf(0) }

    LaunchedEffect(imeInsets) {
        snapshotFlow { imeInsets.getBottom(density) }
            .collect { bottom ->
                height.intValue = bottom
            }
    }

    return height
}
