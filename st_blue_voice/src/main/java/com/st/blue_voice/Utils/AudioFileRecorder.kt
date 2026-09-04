package com.st.blue_voice.utils

import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class AudioFileRecorder(
    var fileSuffix: String,
    var sampleRate: Int = 8000,
    var channels: Short = 1
) {
    private var randomAccessFile: RandomAccessFile? = null
    private var filePath: String? = null
    private var isRecording = false
    private var dataSize = 0L // Tracks total bytes written to the data subchunk

    // Background scope for file I/O to prevent UI stuttering
    private val ioScope = CoroutineScope(Dispatchers.IO)

    fun start() {
        if (isRecording) return

        val dirPath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val directory = File(dirPath, "record")
        if (!directory.exists()) directory.mkdirs()

        val timeStamp = SimpleDateFormat("MMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(directory, "${timeStamp}_${fileSuffix}.wav")
        filePath = file.absolutePath

        randomAccessFile = RandomAccessFile(file, "rw")
        isRecording = true
        dataSize = 0

        ioScope.launch {
            writeWavHeader()
        }
    }

    /**
     * Writes raw PCM ByteArray to the file.
     * Note: Assumes 16-bit PCM (Little Endian).
     */
    fun writeSample(byteArray: ByteArray) {
        if (!isRecording) return
        ioScope.launch {
            randomAccessFile?.let {
                it.seek(it.length()) // Move to end of file
                it.write(byteArray)
                dataSize += byteArray.size
            }
        }
    }

    fun stop(): String? {
        if (!isRecording) return null
        isRecording = false

        ioScope.launch {
            updateWavHeader()
            randomAccessFile?.close()
            randomAccessFile = null
        }
        return filePath
    }

    private fun writeWavHeader() {
        randomAccessFile?.apply {
            writeBytes("RIFF")      // ChunkID
            writeIntLE(0)           // ChunkSize (placeholder)
            writeBytes("WAVE")      // Format
            writeBytes("fmt ")      // Subchunk1ID
            writeIntLE(16)          // Subchunk1Size (16 for PCM)
            writeShortLE(1)         // AudioFormat (1 for PCM)
            writeShortLE(channels)  // NumChannels
            writeIntLE(sampleRate)  // SampleRate
            writeIntLE(sampleRate * channels * 2) // ByteRate
            writeShortLE((channels * 2).toShort()) // BlockAlign
            writeShortLE(16)        // BitsPerSample
            writeBytes("data")      // Subchunk2ID
            writeIntLE(0)           // Subchunk2Size (placeholder)
        }
    }

    private fun updateWavHeader() {
        randomAccessFile?.apply {
            // Update ChunkSize: 36 + SubChunk2Size
            seek(4)
            writeIntLE((36 + dataSize).toInt())

            // Update Subchunk2Size
            seek(40)
            writeIntLE(dataSize.toInt())
        }
    }

    // Helper functions for Little Endian writing
    private fun RandomAccessFile.writeIntLE(value: Int) {
        val buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value)
        write(buffer.array())
    }

    private fun RandomAccessFile.writeShortLE(value: Short) {
        val buffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value)
        write(buffer.array())
    }
}