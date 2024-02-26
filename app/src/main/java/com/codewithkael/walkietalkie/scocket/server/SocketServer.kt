package com.codewithkael.walkietalkie.scocket.server

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

class SocketServer {

    private var socketserver: WebSocketServer? = null
    private val socketClients: MutableList<WebSocket> = mutableListOf()

    private val TAG = "SocketServer"
    fun init(port: Int) {
        if (socketserver == null) {
            socketserver = object : WebSocketServer(InetSocketAddress(port)) {
                override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
                    conn?.let { socketClients.add(it) }
                    Log.d(TAG, "onOpen: new client opened ")
                }

                override fun onClose(
                    conn: WebSocket?,
                    code: Int,
                    reason: String?,
                    remote: Boolean
                ) {
                    conn?.let { socketClients.remove(it) }
                }

                override fun onMessage(conn: WebSocket?, message: String?) {
                    Log.d(TAG, "onMessage: Server Message $message")
                    broadcastMessage(conn, message)
                }

                override fun onError(conn: WebSocket?, ex: Exception?) {
                    ex?.printStackTrace()
                    conn?.let { socketClients.remove(it) }

                }

                override fun onStart() {
                    Log.d(TAG, "onStart: ")
                }


            }.apply { start() }
        }
    }


    fun getSocketAddress(): InetSocketAddress? {
        return socketserver?.address
    }

    private fun broadcastMessage(sender: WebSocket?, message: String?) {
        message?.let { msg ->
            socketClients.forEach { client ->
                if (client != sender) {
                    client.send(msg)
                }
            }
        }
    }

    fun onDestroy() = runCatching { socketserver?.stop() }
}