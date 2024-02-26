package com.codewithkael.walkietalkie.audio

interface AudioStreamer {

    fun startStreaming(onNewByteRecorded: (ByteArray) -> Unit)
    fun stopStreaming()
    fun sendPacketToQue(packet:ByteArray)
}