package com.codewithkael.walkietalkie.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.codewithkael.walkietalkie.utils.Constants.CLIENT_SCREEN
import com.codewithkael.walkietalkie.utils.Constants.SERVER_SCREEN

@Composable
fun HomeScreen(navController: NavHostController) {

    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()

    ) {
        Button(onClick = {
            navController.navigate(SERVER_SCREEN)
        }) {
            Text(text = "Server")
        }

        Button(onClick = {
            navController.navigate(CLIENT_SCREEN)
        }) {
            Text(text = "Client")
        }

    }
}