# OpenAI Streaming API Usage

## Overview

The `OpenAIRepository` provides a streaming implementation for the OpenAI Chat Completions API using Server-Sent Events (SSE).

## Architecture

- **Manual SSE Parsing**: Uses OkHttp's response stream to manually parse SSE format
- **Flow-based API**: Returns `Flow<String>` for reactive stream processing
- **Automatic cleanup**: Cancels the HTTP call when Flow collection stops

## Usage Example

### In a ViewModel

```kotlin
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val openAIRepository: OpenAIRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    fun sendMessage(userMessage: String) {
        viewModelScope.launch {
            // Add user message to history
            _messages.update { it + ChatMessage(role = "user", content = userMessage) }

            // Create request
            val request = ChatRequest(
                model = "gpt-3.5-turbo",
                messages = _messages.value,
                stream = true
            )

            // Collect streaming response
            var fullResponse = ""
            openAIRepository.streamChatCompletion(request)
                .catch { e ->
                    // Handle errors
                    Log.e("ChatViewModel", "Stream error", e)
                }
                .collect { chunk ->
                    // Update UI with each chunk
                    fullResponse += chunk
                    _streamingResponse.value = fullResponse
                }

            // Add complete response to history
            _messages.update {
                it + ChatMessage(role = "assistant", content = fullResponse)
            }
            _streamingResponse.value = ""
        }
    }
}
```

### In a Composable

```kotlin
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val messages by viewModel.messages.collectAsState()
    val streamingResponse by viewModel.streamingResponse.collectAsState()

    LazyColumn {
        items(messages) { message ->
            MessageBubble(message)
        }

        // Show streaming response as it arrives
        if (streamingResponse.isNotEmpty()) {
            item {
                MessageBubble(
                    ChatMessage(role = "assistant", content = streamingResponse)
                )
            }
        }
    }
}
```

## Request Configuration

The `ChatRequest` supports the following parameters:

```kotlin
ChatRequest(
    model = "gpt-3.5-turbo",        // Model name
    messages = listOf(...),          // Conversation history
    stream = true,                   // Enable streaming (always true)
    maxTokens = 150,                 // Optional: Limit response length
    temperature = 0.7,               // Optional: Control randomness (0-2)
    topP = 1.0                       // Optional: Nucleus sampling
)
```

## Error Handling

The Flow will close with an exception in these cases:
- API key is not set
- HTTP request fails
- Response parsing fails
- Network error occurs

Always use `catch` operator to handle errors:

```kotlin
openAIRepository.streamChatCompletion(request)
    .catch { e ->
        when (e) {
            is IOException -> // Network error
            else -> // Other errors
        }
    }
    .collect { chunk -> ... }
```

## Implementation Details

1. **SSE Format**: Parses lines starting with "data: "
2. **End Detection**: Stops when "[DONE]" is received or finishReason is present
3. **Thread Safety**: Uses `callbackFlow` for safe Flow emission from callbacks
4. **Resource Cleanup**: Automatically cancels HTTP call when Flow is cancelled
