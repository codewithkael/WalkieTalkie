//package com.codewithkael.walkietalkie.audio
//
//import android.annotation.SuppressLint
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.media.AudioAttributes
//import android.media.AudioFormat
//import android.media.AudioManager
//import android.media.AudioRecord
//import android.media.AudioTrack
//import android.media.MediaRecorder
//import com.example.bluetoothterminal.service.MainService
//import com.example.bluetoothterminal.utils.Constants
//import com.example.bluetoothterminal.utils.Constants.CODEC2_BUFFER_LIMIT
//import com.example.bluetoothterminal.utils.fromBase64ByteArray
//import com.example.bluetoothterminal.utils.toBase64ByteArray
//import com.ustadmobile.codec2.Codec2
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.io.ByteArrayInputStream
//import java.io.ByteArrayOutputStream
//
//
//class Codec2Streamer(private val audioPacketListener: MainService, context: Context) {
//    private val sampleRate = 8000
//    private val bufferSize = AudioRecord.getMinBufferSize(
//        sampleRate,
//        AudioFormat.CHANNEL_IN_MONO,
//        AudioFormat.ENCODING_PCM_16BIT
//    )
//
//    private var codec2Con: Long = 0
//    private var audioBufferSize = 0
//    private var codec2FrameSize = 0
//    private lateinit var audioRecord: AudioRecord
//    private var audioTrack: AudioTrack? = null
//    private var isStreaming = false
//    private var encodedDataBuffer = ByteArrayOutputStream()
//
//
//    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//    val headphoneReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (AudioManager.ACTION_HEADSET_PLUG == intent.action) {
//                when (intent.getIntExtra("state", -1)) {
//                    0 -> {
//                        // Headset unplugged, switch to speaker mode
//                        updateAudioTrack()
//                        switchAudioInputSource(MediaRecorder.AudioSource.MIC)
//                    }
//
//                    1 -> {
//                        // Headset plugged, switch to handsfree mode
//                        updateAudioTrack()
//                        switchAudioInputSource(MediaRecorder.AudioSource.CAMCORDER) // or VOICE_COMMUNICATION
//                    }
//                }
//            }
//        }
//    }
//
//    init {
//        initCodec()
//        if (isHandsFreeConnected(audioManager)) {
//            initAudioTrack()
//            switchAudioInputSource(MediaRecorder.AudioSource.CAMCORDER) // or VOICE_COMMUNICATION
//        } else {
//            initAudioTrack()
//            switchAudioInputSource(MediaRecorder.AudioSource.MIC)
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            while (true) {
//                delay(Constants.AUDIO_TRACK_RENEWAL_PERIOD)
//                updateAudioTrack()
//            }
//        }
//    }
//
//    private fun isHandsFreeConnected(audioManager: AudioManager): Boolean {
//        return audioManager.isWiredHeadsetOn || audioManager.isBluetoothA2dpOn
//    }
//
//    private fun updateAudioTrack() {
//        try {
//            stopDecoder()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        initAudioTrack()
//    }
//
//    private fun initAudioTrack() {
//        val minBufferSize = AudioTrack.getMinBufferSize(
//            sampleRate,
//            AudioFormat.CHANNEL_OUT_MONO,
//            AudioFormat.ENCODING_PCM_16BIT
//        )
//        audioTrack = AudioTrack.Builder()
//            .setAudioAttributes(
//                AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_MEDIA)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                    .build()
//            )
//            .setAudioFormat(
//                AudioFormat.Builder()
//                    .setSampleRate(sampleRate)
//                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
//                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                    .build()
//            )
//            .setBufferSizeInBytes(minBufferSize)
//            .setTransferMode(AudioTrack.MODE_STREAM)
//            .build()
//        audioTrack?.play()
//    }
//
//
//    @SuppressLint("MissingPermission")
//    private fun switchAudioInputSource(source: Int) {
//        if (::audioRecord.isInitialized) {
//            if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
//                audioRecord.stop()
//            }
//            audioRecord.release()
//        }
//        audioRecord = AudioRecord(
//            source,
//            sampleRate,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            bufferSize
//        )
//        if (isStreaming) {
//            audioRecord.startRecording()
//        }
//    }
//
//    private fun stopDecoder() {
//        audioTrack?.stop()
//        audioTrack?.release()
//        audioTrack = null
//    }
//
//    private fun initCodec() {
//        if (codec2Con != 0L) Codec2.destroy(codec2Con)
//        codec2Con = Codec2.create(Codec2.CODEC2_MODE_1600)
//        audioBufferSize = Codec2.getSamplesPerFrame(codec2Con)
//        codec2FrameSize = Codec2.getBitsSize(codec2Con)
//    }
//
//    fun startStreaming() {
//        Thread {
//            prepareMediaCodec()
//            audioRecord.startRecording()
//            isStreaming = true
//            val shortBuffer = ShortArray(bufferSize / 2) // Each short is 2 bytes
//
//            while (isStreaming) {
//                val bytesRead = audioRecord.read(shortBuffer, 0, bufferSize / 2)
//                if (bytesRead > 0) {
//                    processAudio(shortBuffer)
//                }
//            }
////        stopMediaCodec()
//        }.start()
//    }
//
//    fun stopStreaming() {
//        try {
//            isStreaming = false
//            audioRecord.stop()
//            audioRecord.release()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }
//
//    private fun processAudio(micData: ShortArray) {
//        val encodedData = encodeMicData(micData)
//        emitEncodedData(encodedData)
//    }
//
//    private fun playByteArray(encodedData: ByteArray) {
//        val decodedAudioData = decodeAndPlay(encodedData)
//        audioTrack?.write(decodedAudioData, 0, decodedAudioData.size)
//    }
//
//    private fun encodeMicData(micData: ShortArray): ByteArray {
//        val output = CharArray(codec2FrameSize)
//        Codec2.encode(codec2Con, micData, output)
//        return output.map { it.code.toByte() }.toByteArray()
//    }
//
//    private fun emitEncodedData(encodedData: ByteArray) {
//
//        // Check if the buffer size is greater than or equal to the desired packet size
//        if (isBufferReady()) {
//            emitBufferedPacket()
//        }
//        encodedDataBuffer.write(encodedData)
//
//    }
//
//    private fun isBufferReady(): Boolean {
//        val bufferSize = encodedDataBuffer.size()
//        val requiredSize = CODEC2_BUFFER_LIMIT
//        return bufferSize >= requiredSize
//    }
//
//    private fun emitBufferedPacket() {
//        val packet = encodedDataBuffer.toByteArray()
//        encodedDataBuffer.reset()
//        audioPacketListener.onNewAudioPacketReceived(packet.toBase64ByteArray())
//    }
//
//    private fun decodeAndPlay(encodedData: ByteArray): ShortArray {
//        val data = encodedData.fromBase64ByteArray()
//        val inputStream = ByteArrayInputStream(data)
//        val decodedAudioBuffer = mutableListOf<Short>()
//
//        val codec2Buffer = ByteArray(codec2FrameSize)
//        val playbackAudioBuffer = ShortArray(audioBufferSize)
//
//        while (inputStream.read(codec2Buffer) == codec2FrameSize) {
//            Codec2.decode(codec2Con, playbackAudioBuffer, codec2Buffer)
//            decodedAudioBuffer.addAll(playbackAudioBuffer.toList())
//        }
//
//        return decodedAudioBuffer.toShortArray()
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun prepareMediaCodec() {
//        audioRecord = AudioRecord(
//            MediaRecorder.AudioSource.MIC,
//            sampleRate,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            bufferSize
//        )
//    }
//
//    fun sendToPlayQueue(it: ByteArray) {
//        playByteArray(it)
//    }
//
//
//}