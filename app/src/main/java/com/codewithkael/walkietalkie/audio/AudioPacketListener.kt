package com.example.bluetoothterminal.utils.audio

interface AudioPacketListener {
    fun onNewAudioPacketReceived(byteArray: ByteArray)
}