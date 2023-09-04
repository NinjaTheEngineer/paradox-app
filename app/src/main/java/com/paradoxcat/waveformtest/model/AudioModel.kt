package com.paradoxcat.waveformtest.model

import android.content.res.AssetManager
import java.io.FileInputStream
import java.nio.ByteBuffer

class AudioModel {

    fun loadAudioData(assetManager: AssetManager, fileName: String): ByteBuffer {
        val assetFileDescriptor = assetManager.openFd(fileName)
        val fileDescriptor = assetFileDescriptor.fileDescriptor
        val buffer = ByteBuffer.allocate(assetFileDescriptor.length.toInt())

        FileInputStream(fileDescriptor).use { inputStream ->
            inputStream.channel.use { fileChannel ->
                fileChannel.read(buffer)
            }
        }

        assetFileDescriptor.close()
        return buffer
    }

    fun transformRawData(buffer: ByteBuffer): IntArray {
        val nSamples = buffer.limit() / 2
        val waveForm = IntArray(nSamples)
        for (i in 1 until buffer.limit() step 2) {
            waveForm[i / 2] = (buffer[i].toInt() shl 8) or buffer[i - 1].toInt()
        }
        return waveForm
    }
}