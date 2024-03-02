package com.codewithkael.walkietalkie.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.codewithkael.walkietalkie.audio.MP3AudioStreamer
import com.codewithkael.walkietalkie.scocket.server.SocketClient
import com.codewithkael.walkietalkie.scocket.server.SocketServer
import com.codewithkael.walkietalkie.utils.getWifiIPAddress
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val context: Application,
    private val gson: Gson
) : AndroidViewModel(context), SocketClient.ClientEvents {


    private val socketServer: SocketServer by lazy { SocketServer() }
    private val socketClient: SocketClient by lazy { SocketClient(this) }
    private val mP3AudioStreamer = MP3AudioStreamer()
    private var streamingJob: Job? = null

    //states
    val socketState = MutableStateFlow(false)

    fun startServer(port: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            socketServer.init(port)
            delay(1000)
            startSocketClient("${getWifiIPAddress(context)}:$port")
        }
    }

    fun startSocketClient(serverAddress: String) {
        runCatching {
            socketClient.init(URI.create("ws://$serverAddress"))
        }
    }

    override fun onCleared() {
        socketServer.onDestroy()
        socketClient.onDestroy()
        super.onCleared()
    }

    override fun onIncomingMessage(message: String) {
        mP3AudioStreamer.sendPacketToQue(gson.fromJson(message, ByteArray::class.java))
    }

    override fun onClientSocketOpen() {
        socketState.value = true
    }

    override fun onClientSocketClosed() {
        socketState.value = false
    }

    fun startStreaming() {
        streamingJob?.cancel()
        streamingJob = CoroutineScope(Dispatchers.IO).launch {
            mP3AudioStreamer.startStreaming {
                socketClient.sendPacketsToSocket(gson.toJson(it))
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        runCatching {
            mP3AudioStreamer.stopStreaming()
        }
    }

    fun stopSocketClient() {
        stopStreaming()
        socketClient.onDestroy()
    }

    fun stopServer() {
        socketServer.onDestroy()
        stopStreaming()
    }
}