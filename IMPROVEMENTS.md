# OpenAI Streaming Implementation - Improvements

## 改善内容

OpenAIの公式ドキュメントに基づいて、ストリーミング実装を改善しました。

### 1. **適切なリソース管理**
```kotlin
response.use { resp ->
    responseBody.source().inputStream().bufferedReader().use { reader ->
        parseSSEStream(reader)
    }
}
```
- `use`ブロックで自動的にリソースをクローズ
- メモリリークを防止

### 2. **詳細なエラーハンドリング**
```kotlin
if (!resp.isSuccessful) {
    val errorBody = resp.body?.string() ?: "No error details"
    Log.e(TAG, "HTTP ${resp.code}: $errorBody")
    close(Exception("HTTP ${resp.code}: $errorBody"))
    return
}
```
- HTTPエラーレスポンスのボディを取得して詳細なエラーメッセージを提供
- APIから返されるエラー情報（例：認証エラー、レート制限など）を正確に伝達

### 3. **SSE形式の正確なパース**
```kotlin
when {
    currentLine.startsWith("data: ") -> {
        val data = currentLine.substring(6)
        if (data == "[DONE]") {
            return
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
```
- SSE仕様に準拠したパース（空行、コメント行の処理）
- "[DONE]"マーカーによる明示的なストリーム終了検出

### 4. **包括的なロギング**
```kotlin
Log.d(TAG, "Starting streaming request to OpenAI")
Log.d(TAG, "Received [DONE] marker, ending stream")
Log.d(TAG, "Stream finished with reason: $reason")
Log.e(TAG, "Failed to parse stream chunk: $data", e)
```
- デバッグとトラブルシューティングのための詳細なログ
- エラー発生時の診断が容易

### 5. **finishReasonの適切な処理**
```kotlin
choice.finishReason?.let { reason ->
    Log.d(TAG, "Stream finished with reason: $reason")
    // stop, length, content_filter, tool_calls, など
}
```
- OpenAI APIが返す様々な終了理由に対応
- `stop`: 自然な完了
- `length`: max_tokensに到達
- `content_filter`: コンテンツフィルターによる停止
- `tool_calls`: 関数呼び出しが必要（function calling使用時）

### 6. **エラー時の継続性**
```kotlin
try {
    val streamResponse = json.decodeFromString<ChatStreamResponse>(data)
    // ...
} catch (e: Exception) {
    Log.e(TAG, "Failed to parse stream chunk: $data", e)
    // Don't close the stream on parsing errors, just skip this chunk
}
```
- 単一チャンクのパースエラーでストリーム全体を終了しない
- ネットワークの一時的な問題に対する堅牢性

## OpenAI SSE仕様への準拠

### SSE (Server-Sent Events) 形式
OpenAI APIは以下の形式でストリーミングレスポンスを返します：

```
data: {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1234567890,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"content":"Hello"},"finish_reason":null}]}

data: {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1234567890,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"content":" world"},"finish_reason":null}]}

data: {"id":"chatcmpl-123","object":"chat.completion.chunk","created":1234567890,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{},"finish_reason":"stop"}]}

data: [DONE]
```

### 主要な特徴
1. 各データ行は`data: `で始まる
2. 空行がイベントを区切る
3. `:`で始まる行はコメント（無視）
4. 最後に`data: [DONE]`で終了

## 使用例

### ViewModelでの使用
```kotlin
viewModelScope.launch {
    val request = ChatRequest(
        model = "gpt-4",
        messages = listOf(
            ChatMessage(role = "system", content = "You are a helpful assistant."),
            ChatMessage(role = "user", content = "Tell me a joke")
        ),
        maxTokens = 150,
        temperature = 0.7
    )

    openAIRepository.streamChatCompletion(request)
        .catch { e ->
            when {
                e.message?.contains("HTTP 401") == true -> {
                    // API key is invalid
                    _error.value = "無効なAPIキーです"
                }
                e.message?.contains("HTTP 429") == true -> {
                    // Rate limit exceeded
                    _error.value = "レート制限に達しました"
                }
                else -> {
                    _error.value = "エラー: ${e.message}"
                }
            }
        }
        .collect { chunk ->
            // リアルタイムでチャンクを表示
            currentResponse += chunk
            _streamingText.value = currentResponse
        }
}
```

## テストのヒント

### Logcatでの確認
```bash
adb logcat -s OpenAIRepository
```

実際のストリーミング動作を確認できます：
```
D/OpenAIRepository: Starting streaming request to OpenAI
D/OpenAIRepository: Stream finished with reason: stop
D/OpenAIRepository: Stream completed successfully
```

### エラー時の詳細情報
```
E/OpenAIRepository: HTTP 401: {
  "error": {
    "message": "Incorrect API key provided",
    "type": "invalid_request_error",
    "param": null,
    "code": "invalid_api_key"
  }
}
```

## パフォーマンス

- **メモリ効率**: ストリーミングにより、大きなレスポンスを一度にメモリに読み込まない
- **レスポンス時間**: 最初のトークンが到着次第、UIに表示開始
- **リソース管理**: 自動的なクリーンアップによりリソースリークなし
