package org.nunocky.androidaichatclientbasic.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Context.dataStore の定義（ファイル名: ai_chat_prefs）
private val Context.dataStore by preferencesDataStore(name = "ai_chat_prefs")

class PreferencesDataStoreImpl @Inject constructor(
    private val context: Context
) : DataStoreRepository {

    private val API_KEY = stringPreferencesKey("api_key")

    override val apiKeyFlow: Flow<String?> = context.dataStore.data
        .map { prefs -> prefs[API_KEY] }

    override suspend fun setApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[API_KEY] = key
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

