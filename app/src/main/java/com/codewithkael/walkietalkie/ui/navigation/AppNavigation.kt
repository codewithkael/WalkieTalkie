package com.codewithkael.walkietalkie.ui.navigation

import HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codewithkael.walkietalkie.ui.screen.ClientScreen
import com.codewithkael.walkietalkie.ui.screen.ServerScreen
import com.codewithkael.walkietalkie.utils.Constants.CLIENT_SCREEN
import com.codewithkael.walkietalkie.utils.Constants.MAIN_SCREEN
import com.codewithkael.walkietalkie.utils.Constants.SERVER_SCREEN

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MAIN_SCREEN) {
        composable(MAIN_SCREEN) {
            HomeScreen(
                navController = navController
            )
        }
        composable(SERVER_SCREEN) {
            ServerScreen()
        }

        composable(CLIENT_SCREEN) {
            ClientScreen()
        }
    }
}