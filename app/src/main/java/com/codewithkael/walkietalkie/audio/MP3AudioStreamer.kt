package com.codewithkael.walkietalkie.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.*
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class MP3AudioStreamer : AudioStreamer {


    private val sampleRate = 16000 // Use a higher sample rate for better quality
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private var isRecording = false
    private val playDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val recordDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private fun createAudioRecord() {
        audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .build()
    }

    private fun createAudioTrack() {
        audioTrack = AudioTrack.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .build()
    }

    init {
        createAudioTrack()
        audioTrack?.play()
    }

    override fun startStreaming(onNewByteRecorded: (ByteArray) -> Unit) {
        createAudioRecord()
        audioRecord?.startRecording()
        isRecording = true
        CoroutineScope(recordDispatcher).launch {
            val buffer = ByteArray(minBufferSize)
            while (isRecording) {
                audioRecord?.let { record ->
                    val readSize = record.read(buffer, 0, buffer.size)
                    if (readSize > 0) {
                        onNewByteRecorded(buffer)
                    }
                }
            }
        }
    }

    override fun stopStreaming() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    override fun sendPacketToQue(packet: ByteArray) {
        CoroutineScope(playDispatcher).launch {
            audioTrack?.write(packet, 0, packet.size)
        }
    }
}
