package org.nunocky.androidaichatclientbasic

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
object NavHome

@Composable
fun MyAppNavigation() {
    val activity = LocalActivity.current
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavHome,
    ) {
        composable<NavHome> {
            HomeScreen(
                navigationBack = {
                    activity?.finish()
                })
        }
    }
}