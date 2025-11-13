package com.example.xml_example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ccaiplatform.ccaichat.chatService
import com.ccaiplatform.ccaichat.model.ChatResponse
import com.ccaiplatform.ccaikit.CCAI
import co.ccai.example.xml_example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _showChatView = MutableLiveData<Boolean>()
    val showChatView: LiveData<Boolean> = _showChatView

    private val _errorToast = MutableLiveData<String>()
    val errorToast: LiveData<String> = _errorToast

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _chat = MutableLiveData<ChatResponse?>()
    val chat: LiveData<ChatResponse?> = _chat

    var menuId: String = ""
    var context = getApplication<Application>()

    fun startContactCustomerSupport() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chatResult = withContext(Dispatchers.IO) {
                    CCAI.chatService?.getLastChatInProgress()
                }
                if (chatResult != null) {
                    _chat.value = chatResult
                    menuId = chatResult.menus?.lastOrNull()?.id?.toString() ?: "0"
                    _showChatView.value = true
                } else {
                    menuId.toIntOrNull()?.let {
                        _showChatView.value = true
                    } ?: run {
                        _errorToast.value = context.getString(R.string.please_enter_valid_menu_id)
                        _showChatView.value = false
                    }
                }
            } catch (e: Exception) {
                _errorToast.value = e.message ?: context.getString(R.string.an_error_occurred)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetShowChatView() {
        _errorToast.value = ""
        _showChatView.value = false
    }
} 
