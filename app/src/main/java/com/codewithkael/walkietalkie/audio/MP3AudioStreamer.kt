package com.codewithkael.walkietalkie.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import java.util.concurrent.Executors

@SuppressLint("MissingPermission")
class MP3AudioStreamer() : AudioStreamer {

    private val executor = Executors.newSingleThreadExecutor()

    private val sampleRate = 4000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        sampleRate,
        channelConfig,
        audioFormat,
        minBufferSize
    )
    private val audioTrack = AudioTrack.Builder()
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(audioFormat)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(minBufferSize)
        .build()

    private var isRecording = false

    init {
        startPlaying()
    }


    private fun startPlaying() {
        executor.execute {
            audioTrack.play()
        }
    }

    fun stopPlaying() {
        executor.execute {
            audioTrack.stop()
            audioTrack.release()
        }
    }

    override fun startStreaming(onNewByteRecorded: (ByteArray) -> Unit) {
        executor.execute {
            audioRecord.startRecording()
            isRecording = true

            val buffer = ByteArray(minBufferSize)
            while (isRecording) {

                val readSize = audioRecord.read(buffer, 0, buffer.size)
                if (readSize > 0) {
                    onNewByteRecorded(buffer)
                }
            }
        }
    }

    override fun stopStreaming() {
        isRecording = false
        audioRecord.stop()
        audioRecord.release()
    }

    override fun sendPacketToQue(packet: ByteArray) {
        executor.execute {
            audioTrack.write(packet, 0, packet.size)
        }
    }


}