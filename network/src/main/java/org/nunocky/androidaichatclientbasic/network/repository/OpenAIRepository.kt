package org.nunocky.androidaichatclientbasic.network.repository

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.nunocky.androidaichatclientbasic.network.model.ChatRequest
import org.nunocky.androidaichatclientbasic.network.model.ChatStreamResponse
import java.io.BufferedReader
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for interacting with OpenAI API
 */
@Singleton
class OpenAIRepository @Inject constructor(
    private val okHttpClient: OkHttpClient, private val json: Json
) {
    private val baseUrl = "https://api.openai.com/v1"

    /**
     * Stream chat completion from OpenAI API using Server-Sent Events (SSE)
     *
     * @param chatRequest The chat request containing messages and model configuration
     * @return Flow of content strings as they arrive from the API
     * @throws Exception if API key is not set, network error, or API returns an error
     */
    fun streamChatCompletion(
        apiKey: String, chatRequest: ChatRequest
    ): Flow<String> = callbackFlow {

        if (apiKey.isBlank()) {
//            Log.e(TAG, "API Key is not set")
            close(Exception("API Key is not set"))
            return@callbackFlow
        }

        // Ensure streaming is enabled
        val streamingRequest = chatRequest.copy(stream = true)

        val jsonString = json.encodeToString(ChatRequest.serializer(), streamingRequest)
//        Log.d(TAG, "Request JSON: $jsonString")

        val requestBody = jsonString.toRequestBody("application/json".toMediaType())

        val request = Request.Builder().url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json").post(requestBody).build()

        //Log.d(TAG, "Starting streaming request to OpenAI")
        val call = okHttpClient.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
//                Log.e(TAG, "Network request failed", e)
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    try {
                        if (!resp.isSuccessful) {
                            val errorBody = resp.body?.string() ?: "No error details"
//                            Log.e(TAG, "HTTP ${resp.code}: $errorBody")
                            close(Exception("HTTP ${resp.code}: $errorBody"))
                            return
                        }

                        val responseBody = resp.body
                        if (responseBody == null) {
//                            Log.e(TAG, "Response body is null")
                            close(Exception("Response body is null"))
                            return
                        }

                        // Read the SSE stream line by line
                        responseBody.source().inputStream().bufferedReader().use { reader ->
                            parseSSEStream(reader)
                        }

//                        Log.d(TAG, "Stream completed successfully")
                        close()
                    } catch (e: Exception) {
//                        Log.e(TAG, "Error processing response", e)
                        close(e)
                    }
                }
            }

            private fun parseSSEStream(reader: BufferedReader) {
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue

                    // SSE format: lines starting with "data: " followed by JSON or [DONE]
                    when {
                        currentLine.startsWith("data: ") -> {
                            val data = currentLine.substring(6)

                            // Check for stream end marker
                            if (data == "[DONE]") {
//                                Log.d(TAG, "Received [DONE] marker, ending stream")
                                return
                            }

                            // Skip empty data lines
                            if (data.isBlank()) {
                                continue
                            }

                            processStreamChunk(data)
                        }

                        currentLine.isEmpty() -> {
                            // Empty lines separate events in SSE format
                            continue
                        }

                        currentLine.startsWith(":") -> {
                            // Comment line, ignore
                            continue
                        }
                    }
                }
            }

            private fun processStreamChunk(data: String) {
                try {
                    val streamResponse = json.decodeFromString<ChatStreamResponse>(data)
                    val choice = streamResponse.choices.firstOrNull()

                    if (choice != null) {
                        // Emit content if available
                        val content = choice.delta.content
                        if (!content.isNullOrEmpty()) {
                            val sendResult = trySend(content)
                            if (sendResult.isFailure) {
//                                Log.w(TAG, "Failed to send content chunk")
                            }
                        }

                        // Check for finish reason (stop, length, content_filter, etc.)
                        choice.finishReason?.let { reason ->
//                            Log.d(TAG, "Stream finished with reason: $reason")
                            // Stream is complete, no need to continue
                        }
                    }
                } catch (e: Exception) {
//                    Log.e(TAG, "Failed to parse stream chunk: $data", e)
                    // Don't close the stream on parsing errors, just skip this chunk
                }
            }
        })

        awaitClose {
//            Log.d(TAG, "Flow collection cancelled, cancelling HTTP call")
            call.cancel()
        }
    }

    companion object {
        private const val TAG = "OpenAIRepository"
    }
}
