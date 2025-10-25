package org.nunocky.androidaichatclientbasic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.nunocky.androidaichatclientbasic.ui.theme.AndroidAIChatClientBasicTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidAIChatClientBasicTheme {
                MyAppNavigation()
            }
        }
    }
}

