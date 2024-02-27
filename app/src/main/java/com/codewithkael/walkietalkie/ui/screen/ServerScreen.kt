package com.codewithkael.walkietalkie.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.codewithkael.walkietalkie.R
import com.codewithkael.walkietalkie.ui.MainViewModel
import com.codewithkael.walkietalkie.utils.getWifiIPAddress

@Composable
fun ServerScreen(navController: NavHostController) {
    val viewModel: MainViewModel = hiltViewModel()
    val context = LocalContext.current

    val socketState = viewModel.socketState.collectAsState()

    viewModel.init(true)
    var isToggledOn by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Text(text = getWifiIPAddress(context) ?: "Failed to retrieve ip")
            Spacer(modifier = Modifier.padding(5.dp))
            Text(text = "Socket is ${if (socketState.value) "Connected" else "Disconnected"}")
            Spacer(modifier = Modifier.padding(5.dp))
            Icon(
                painter = if (isToggledOn) painterResource(id = R.drawable.ic_mic_off) else painterResource(
                    id = R.drawable.ic_mic
                ),
                contentDescription = if (isToggledOn) "Stop Streaming" else "Start Streaming",
                tint = Color.White,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .padding(4.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 5.dp, vertical = 10.dp)
                    .size(40.dp) // Adjust size as needed
                    .clickable {
                        isToggledOn = !isToggledOn
                        if (isToggledOn) {
                            viewModel.startStreaming()
                        } else {
                            viewModel.stopStreaming()
                        }
                    }
            )
        }
    }
}