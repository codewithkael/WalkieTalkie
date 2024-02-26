package com.codewithkael.walkietalkie.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaRecorder
import android.util.Log
import com.example.bluetoothterminal.utils.audio.AudioPacketListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.math.min

class AmrAudioStreamer(
    private val audioPacketListener: AudioPacketListener,
    context: Context
) {

    private val audioSingleDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()


    private var decoderIsStarted: Boolean = false

    private val sampleRate = 8000  // Common sample rate for AMR
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    private lateinit var audioRecord: AudioRecord
    private lateinit var mediaCodec: MediaCodec
    private var isStreaming = false
    private val mediaFormat =
        MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AMR_NB, sampleRate, 1).apply {
            setInteger(
                MediaFormat.KEY_BIT_RATE, 4750
            ) // Set lowest bitrate for AMR-NB
        }

    private val playingBytesQue = mutableListOf<ByteArray>()

    init {

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(120000)
                updateAudioTrack()
            }
        }
        CoroutineScope(audioSingleDispatcher).launch {
            while (true) {
//                Log.d(TAG, "queue size is here ${playingBytesQue.size}: ")
                if (playingBytesQue.size >= 2) {
                    repeat(playingBytesQue.size) {
                        async {
                            try {
                                playByteArray(playingBytesQue.removeAt(0))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            Log.d("TAG", "escaped from que -- iterated ${playingBytesQue.size} ")

                            if (playingBytesQue.size < 1) {
                                Log.d("TAG", "escaped from que ")
                                return@async
                            }
                        }.await()

                    }
                } else {
                    delay(300)
                }
            }
        }
    }

    private fun initAudioTrack() {
        initDecoder()
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        audioTrack?.play()
    }

    private fun updateAudioTrack() {
        try {
            stopDecoder()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initAudioTrack()
    }

    @SuppressLint("MissingPermission")
    private fun switchAudioInputSource(source: Int) {
        if (::audioRecord.isInitialized) {
            if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop()
            }
            audioRecord.release()
        }
        audioRecord = AudioRecord(
            source,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        if (isStreaming) {
            audioRecord.startRecording()
        }
    }

    private fun isHandsFreeConnected(audioManager: AudioManager): Boolean {
        return audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn
    }

    fun startStreaming() {
        Thread {
            prepareMediaCodec()
            audioRecord.startRecording()
            isStreaming = true
            while (isStreaming) {
                val inputBuffer = ByteBuffer.allocateDirect(bufferSize)
                val bytesRead = audioRecord.read(inputBuffer, bufferSize)
                if (bytesRead > 0) {
                    processAudio(inputBuffer, bytesRead)
                }
            }
            stopMediaCodec()
        }.start()

    }

    @SuppressLint("MissingPermission")
    private fun prepareMediaCodec() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AMR_NB)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec.start()
    }

    private var encodedDataBuffer = ByteArrayOutputStream()
    private fun processAudio(inputBuffer: ByteBuffer, length: Int) {
        val inputIndex = mediaCodec.dequeueInputBuffer(-1)
        if (inputIndex >= 0) {
            val inputByteBuffer = mediaCodec.getInputBuffer(inputIndex)
            inputByteBuffer?.let { buffer ->
                buffer.clear()

                // Calculate the amount of data we can put in the buffer
                val bytesToWrite = min(buffer.remaining(), length)

                if (bytesToWrite > 0) {
                    val tempBuffer = ByteArray(bytesToWrite)
                    inputBuffer.get(tempBuffer)
                    buffer.put(tempBuffer)
                    mediaCodec.queueInputBuffer(inputIndex, 0, bytesToWrite, 0, 0)
                }
            }
        }

        val bufferInfo = MediaCodec.BufferInfo()
        var outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
        while (outputIndex >= 0) {
            val outputBuffer = mediaCodec.getOutputBuffer(outputIndex)
            outputBuffer?.let { buffer ->
                val encodedData = ByteArray(bufferInfo.size)
                buffer.get(encodedData)
                buffer.clear()

//                if ((encodedDataBuffer.size() + encodedData.size)*118 /100 >= CORE_PACKET_SIZE) {
//                if ((encodedDataBuffer.size() + encodedData.size) * 4 / 3 >= CORE_PACKET_SIZE) {
//                    val packet = encodedDataBuffer.toByteArray()
//                    encodedDataBuffer.reset()
//
//
//                    audioPacketListener.onNewAudioPacketReceived(packet.toBase64ByteArray())
//                    // Now 'packet' contains up to DESIRED_PACKET_SIZE bytes of AMR data
//                    // Send or process this packet
//                } else {
//                    encodedDataBuffer.write(encodedData)
//                }
            }
            mediaCodec.releaseOutputBuffer(outputIndex, false)
            outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
        }
    }

    private var decoder: MediaCodec? = null
    private var audioTrack: AudioTrack? = null

    private fun initDecoder() {
        decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AMR_NB)
        decoder?.configure(mediaFormat, null, null, 0)
        decoder?.start()
        decoderIsStarted = true

    }

    fun sendToPlayQueue(encodedData: ByteArray) {
        playingBytesQue.add(encodedData)
    }

    private fun playByteArray(encodedData2: ByteArray) {
//        val encodedData = encodedData2.fromBase64ByteArray()
        val encodedData = encodedData2
        val inputBufferIndex = decoder?.dequeueInputBuffer(1000)
        if (inputBufferIndex != null) {
            if (inputBufferIndex >= 0) {
                val inputBuffer = decoder?.getInputBuffer(inputBufferIndex)
                inputBuffer?.clear()
                inputBuffer?.put(encodedData)
                decoder?.queueInputBuffer(inputBufferIndex, 0, encodedData.size, 0, 0)
            }
        }

        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = decoder?.dequeueOutputBuffer(bufferInfo, 0)
        if (outputBufferIndex != null) {
            while (outputBufferIndex!! >= 0) {
                val outputBuffer = decoder?.getOutputBuffer(outputBufferIndex)
                val decodedAudioData = ByteArray(bufferInfo.size)
                outputBuffer?.get(decodedAudioData)
                outputBuffer?.clear()

                audioTrack?.write(decodedAudioData, 0, decodedAudioData.size)
                decoder?.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = decoder?.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
    }

    private fun stopDecoder() {
        decoderIsStarted = false
        decoder?.stop()
        decoder?.release()
        decoder = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }


    fun stopStreaming() {
        try {
            isStreaming = false
            audioRecord.stop()
            audioRecord.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun stopMediaCodec() {
        mediaCodec.stop()
        mediaCodec.release()
    }
}
