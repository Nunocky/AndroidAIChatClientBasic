package org.nunocky.androidaichatclientbasic.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.nunocky.androidaichatclientbasic.datastore.DataStoreRepository
import org.nunocky.androidaichatclientbasic.network.model.ChatMessage
import org.nunocky.androidaichatclientbasic.network.model.ChatRequest
import org.nunocky.androidaichatclientbasic.network.repository.OpenAIRepository
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val repository: OpenAIRepository,
) : ViewModel() {
    private val _history = MutableStateFlow<List<ChatMessage>>(emptyList())
    val history = _history.asStateFlow()

    private val _currentOutput = MutableStateFlow<String>("")
    val currentOutput = _currentOutput.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun doConversation(text: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val apiKey = dataStoreRepository.apiKeyFlow.first() ?: ""

            val userMessage = ChatMessage(role = "user", content = text)
            val newHistory = _history.value.toMutableList().apply { add(userMessage) }
            _history.value = newHistory

            _currentOutput.value = ""

            try {
                val chatRequest = ChatRequest(
                    model = "gpt-4o", messages = _history.value
                )

                repository.streamChatCompletion(
                    apiKey = apiKey,
                    chatRequest = chatRequest,
                ).collect { chunk ->
                    _currentOutput.value += chunk
                }

                val assistantMessage = ChatMessage(
                    role = "assistant", content = _currentOutput.value
                )
                _history.value = _history.value + assistantMessage

                _currentOutput.value = ""
            } catch (e: Exception) {
                _currentOutput.value = ""
                _snackbarMessage.emit("エラー: ${e.message ?: "通信に失敗しました"}")
            }
        }
    }
}