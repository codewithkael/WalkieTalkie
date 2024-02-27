@file:Suppress("DEPRECATION")

package com.codewithkael.walkietalkie.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteOrder

fun getWifiIPAddress(context: Context): String? {
    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    var ipAddress = wifiManager.connectionInfo.ipAddress

    // Convert little-endian to big-endian if needed
    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
        ipAddress = Integer.reverseBytes(ipAddress)
    }

    val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()

    return try {
        InetAddress.getByAddress(ipByteArray).hostAddress
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
}
//
//// Function to find the WebSocket server using coroutines for concurrency
//fun findWebSocketServerCoroutine(
//    scope: CoroutineScope,
//    port: Int,
//    onServerFound: (String) -> Unit
//) {
//    val subnet = "192.168.1"
//    val startIp = 1
//    val endIp = 254
//    // Launch coroutines within the given scope
//    scope.launch {
//        val jobs = (startIp..endIp).map { ip ->
//            async(Dispatchers.IO) {
//                val testIp = "$subnet.$ip"
//                try {
//                    Socket().use { socket ->
//                        socket.connect(InetSocketAddress(testIp, port), 100) // 100ms timeout
//                        testIp // Return IP address if connection is successful
//                    }
//                } catch (e: Exception) {
//                    null // Return null if connection fails
//                }
//            }
//        }.awaitAll()
//            .filterNotNull() // Await all coroutines and filter out nulls (failed connections)
//        // Take the first successful IP address, if any
//        jobs.firstOrNull()?.let { serverIp ->
//            withContext(Dispatchers.Main) {
//                onServerFound(serverIp)
//            }
//        }
//    }
//}
//
//// Function to concurrently scan all IPs in the subnet
//fun scanNetworkConcurrently(
//    networkScanScope: CoroutineScope,
//    port: Int,
//    onServerFound: (String) -> Unit
//) {
//    networkScanScope.launch {
//        val addresses = (1..255).map { ip ->
//            async {
//                val testIp = "192.168.1.$ip"
//                try {
//                    Socket().use { socket ->
//                        socket.connect(InetSocketAddress(testIp, port), 100) // Short timeout
//                        testIp
//                    }
//                } catch (e: Exception) {
//                    "" // Return an empty string or null upon failure
//                }
//            }
//        }.awaitAll()
//
//        // Filter out unsuccessful attempts and process successful ones
//        addresses.filter { it.isNotEmpty() }.forEach(onServerFound)
//    }
//}
//
