package org.nunocky.androidaichatclientbasic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.nunocky.androidaichatclientbasic.datastore.DataStoreRepository
import javax.inject.Inject

@HiltViewModel
class SettingApiKeyViewModel @Inject constructor(
    val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    suspend fun setApiKey(newValue: String) {
        dataStoreRepository.setApiKey(newValue)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingApiKeyScreen(
    navigateBack: () -> Unit = {}, viewModel: SettingApiKeyViewModel = hiltViewModel()
) {
    val apiKey by viewModel.dataStoreRepository.apiKeyFlow.collectAsState(null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Key") },
                navigationIcon = {
                    IconButton(onClick = {
                        navigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
                    }
                },
            )
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            val displayText = apiKey ?: ""
            val scope = rememberCoroutineScope()

            OutlinedTextField(
                value = displayText,
                onValueChange = { newValue ->
                    scope.launch {
                        viewModel.setApiKey(newValue)
                    }
                },
                label = { Text("API Key") },
                modifier = Modifier.padding(top = 16.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }
    }
}