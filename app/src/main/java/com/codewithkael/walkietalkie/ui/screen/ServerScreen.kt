package com.codewithkael.walkietalkie.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.codewithkael.walkietalkie.ui.MainViewModel
import com.codewithkael.walkietalkie.utils.getWifiIPAddress

@Composable
fun ServerScreen(navController: NavHostController) {
    val viewModel:MainViewModel = hiltViewModel()
    val context = LocalContext.current

    val socketState = viewModel.socketState.collectAsState()

    viewModel.init(true)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(text = getWifiIPAddress(context) ?: "Failed to retrieve ip")
            Spacer(modifier = Modifier.padding(5.dp))
            Text(text = "Socket is ${if (socketState.value) "Connected" else "Disconnected"}")

        }
    }
}