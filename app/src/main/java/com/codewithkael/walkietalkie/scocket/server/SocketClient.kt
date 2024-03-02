package com.codewithkael.walkietalkie.scocket.server

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketClient(private val eventListener: ClientEvents) {

    private var socketClient: WebSocketClient? = null
    private val TAG = "SocketClient"

    fun init(serverUri: URI) {
        Log.d(TAG, "init: $serverUri")
        socketClient = object : WebSocketClient(serverUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                eventListener.onClientSocketOpen()
                Log.d(TAG, "onOpen: ")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose: ")
                eventListener.onClientSocketClosed()
            }

            override fun onMessage(message: String?) {
                message?.let { eventListener.onIncomingMessage(it) }
            }

            override fun onError(ex: Exception?) {
                Log.d(TAG, "onError: ${ex?.message}")
                eventListener.onClientSocketClosed()
            }
        }
        socketClient?.connect()
    }

    fun sendPacketsToSocket(data: String) {
        runCatching {
            socketClient?.send(data)
        }
    }

    fun onDestroy() {
        runCatching {
            socketClient?.close()
            socketClient = null
        }
    }

    interface ClientEvents {
        fun onIncomingMessage(message: String)
        fun onClientSocketOpen()
        fun onClientSocketClosed()
    }
}