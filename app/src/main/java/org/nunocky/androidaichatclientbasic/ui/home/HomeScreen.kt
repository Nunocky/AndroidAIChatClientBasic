package org.nunocky.androidaichatclientbasic.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateSetApiKey: () -> Unit = {},
    navigateBack: () -> Unit = {},
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val textFieldState = rememberTextFieldState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val history by viewModel.history.collectAsState()
    val currentOutput by viewModel.currentOutput.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BackHandler {
        navigateBack()
    }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Text("API Key", modifier = Modifier.clickable {
                    navigateSetApiKey()
                })
            }
        }) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("AndroidAIChatBasic") },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "menu")
                    }
                },
            )
        }, snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                // History
                LazyColumn {
                    items(history) {
                        Text("${it.role}: ${it.content}")
                    }
                }

                // Current output
                Text(currentOutput)

                Spacer(Modifier.weight(1f))

                // Input
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        state = textFieldState,
                        placeholder = { Text("input text") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        onKeyboardAction = { keyboardController?.hide() },
                        shape = RoundedCornerShape(100.dp)
                    )

                    IconButton(onClick = {
                        val text = textFieldState.text.toString()
                        if (text.isNotBlank()) {
                            viewModel.doConversation(text)
                            textFieldState.edit {
                                replace(0, length, "")
                            }
                            keyboardController?.hide()
                        }
                    }) {
                        Icon(
                            Icons.Default.Start,
                            contentDescription = "send",
                        )
                    }
                }
            }
        }
    }
}