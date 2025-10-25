package org.nunocky.androidaichatclientbasic

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.nunocky.androidaichatclientbasic.ui.home.HomeScreen

@Serializable
object NavHome

@Serializable
object NavSettingApiKey

@Composable
fun MyAppNavigation() {
    val activity = LocalActivity.current
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavHome,
    ) {
        composable<NavHome> {
            HomeScreen(navigateSetApiKey = {
                navController.navigate(NavSettingApiKey)
            }, navigateBack = {
                activity?.finish()
            })
        }
        composable<NavSettingApiKey> {
            SettingApiKeyScreen(
                navigateBack = {
                    navController.popBackStack()
                })
        }
    }
}