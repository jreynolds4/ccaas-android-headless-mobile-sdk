package com.example.ccaiexample.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.ccaiplatform.ccaichat.chatService
import com.ccaiplatform.ccaichat.model.ChatResponse
import com.ccaiplatform.ccaikit.CCAI
import co.ccai.example.compose_example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    var showChatView by mutableStateOf(false)
        private set

    var errorToast by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var chat by mutableStateOf<ChatResponse?>(null)
        private set

    var menuId: String = ""

    private val application = getApplication<Application>()

    fun startContactCustomerSupport() {
        viewModelScope.launch {
            isLoading = true
            try {
                val chatResult = withContext(Dispatchers.IO) {
                    CCAI.chatService?.getLastChatInProgress()
                }
                if (chatResult != null) {
                    chat = chatResult
                    menuId = chatResult.menus?.lastOrNull()?.id?.toString() ?: "0"
                    showChatView = true
                } else {
                    menuId.toIntOrNull()?.let {
                        showChatView = true
                    } ?: run {
                        errorToast = application.getString(R.string.please_enter_valid_menu_id)
                        showChatView = false
                    }
                }
            } catch (e: Exception) {
                errorToast = e.message ?: "An error occurred"
            } finally {
                isLoading = false
            }
        }
    }

    fun resetShowChatView() {
        errorToast = ""
        showChatView = false
    }
}
