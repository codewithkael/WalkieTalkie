package com.codewithkael.walkietalkie.ui.screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.codewithkael.walkietalkie.ui.MainViewModel
import com.codewithkael.walkietalkie.ui.screen.components.MicToggleButton

@Composable
fun ClientScreen() {
    val viewModel: MainViewModel = hiltViewModel()

    val context = LocalContext.current
    // Observing socket state
    val socketState by viewModel.socketState.collectAsState()

    // Target IP and Port
    var targetIp by remember { mutableStateOf("") }
    var targetPort by remember { mutableStateOf("3001") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startSocketClient("$targetIp:$targetPort")
        } else {
            // Handle permission denied
            Toast.makeText(context, "Mic permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.padding(5.dp))

        // Display socket connection status
        Text(
            text = "Socket is ${if (socketState) "Connected" else "Disconnected"}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.padding(5.dp))

        // TextFields for IP and Port
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = targetIp,
                onValueChange = { targetIp = it },
                label = { Text("Server ip address") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(8.dp)
            )
            TextField(
                value = targetPort,
                onValueChange = { targetPort = it.filter { char -> char.isDigit() } },
                label = { Text("Port address") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.padding(5.dp))

        // Button to connect/disconnect socket
        Button(
            onClick = {
                if (socketState) {
                    viewModel.stopSocketClient()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(text = if (socketState) "Disconnect" else "Connect")
        }

        // Mic Toggle Button

        if (socketState) {
            MicToggleButton(
                onToggle = {
                    if (it) {
                        viewModel.startStreaming()
                    } else {
                        viewModel.stopStreaming()
                    }
                }
            )
        }
    }
}

