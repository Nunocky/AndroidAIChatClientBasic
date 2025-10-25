package org.nunocky.androidaichatclientbasic.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateSetApiKey: () -> Unit = {}, navigateBack: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope() // CoroutineScopeも必要

    val textFieldState = rememberTextFieldState()
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler {
        navigateBack()
    }

    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Text("API Key", modifier = Modifier.clickable() {
                    navigateSetApiKey()
                })
            }
        }) {
        Scaffold(
            topBar = {
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
            }) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                // Chat History
                Text("history")
//                LazyColumn(history) { item
//                }
                Spacer(Modifier.weight(1f))

                // Input
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        state = textFieldState,
                        placeholder = { Text("input text") },
                        //lineLimits = TextFieldLineLimits.SingleLine,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        onKeyboardAction = { keyboardController?.hide() },
                        shape = RoundedCornerShape(100.dp)
                    )

                    // right arrow
                    IconButton(onClick = {
                        // TODO : send
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