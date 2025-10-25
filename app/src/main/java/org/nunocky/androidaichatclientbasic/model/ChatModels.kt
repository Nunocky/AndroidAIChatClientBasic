package org.nunocky.androidaichatclientbasic.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for OpenAI Chat Completions API
 */
@Serializable
data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    val stream: Boolean = true,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val temperature: Double? = null,
    @SerialName("top_p") val topP: Double? = null
)

/**
 * Message in the conversation
 */
@Serializable
data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String
)

/**
 * Streaming response chunk from OpenAI API
 */
@Serializable
data class ChatStreamResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatStreamChoice>
)

/**
 * Choice in the streaming response
 */
@Serializable
data class ChatStreamChoice(
    val index: Int,
    val delta: ChatDelta,
    @SerialName("finish_reason") val finishReason: String? = null
)

/**
 * Delta content in streaming response
 */
@Serializable
data class ChatDelta(
    val role: String? = null,
    val content: String? = null
)
