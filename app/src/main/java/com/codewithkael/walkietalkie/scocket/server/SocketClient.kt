package com.codewithkael.walkietalkie.scocket.server

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketClient(private val eventListener: ClientEvents) {

    private var socketClient: WebSocketClient? = null

    fun init(serverUri: URI) {
        socketClient = object : WebSocketClient(serverUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                eventListener.onClientSocketOpen()
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                eventListener.onClientSocketClosed()
            }

            override fun onMessage(message: String?) {
                message?.let { eventListener.onIncomingMessage(it) }
            }

            override fun onError(ex: Exception?) {
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