package org.nunocky.androidaichatclientbasic.datastore

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    // 保存された API キーを監視する Flow（null 可能）
    val apiKeyFlow: Flow<String?>

    // API キーを保存
    suspend fun setApiKey(key: String)

    // 全データをクリア
    suspend fun clear()
}

