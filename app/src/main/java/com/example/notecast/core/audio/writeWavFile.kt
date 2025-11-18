package com.example.notecast.core.audio

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int = 16000) {
    FileOutputStream(file).use { out ->
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val dataLen = pcm.size * 2
        val totalDataLen = 36 + dataLen

        out.write("RIFF".toByteArray())
        out.write(intToByteArrayLE(totalDataLen))
        out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray())
        out.write(intToByteArrayLE(16))
        out.write(shortToByteArrayLE(1))
        out.write(shortToByteArrayLE(channels.toShort()))
        out.write(intToByteArrayLE(sampleRate))
        out.write(intToByteArrayLE(byteRate))
        out.write(shortToByteArrayLE((channels * bitsPerSample / 8).toShort()))
        out.write(shortToByteArrayLE(bitsPerSample.toShort()))
        out.write("data".toByteArray())
        out.write(intToByteArrayLE(dataLen))

        val buf = ByteBuffer.allocate(2 * pcm.size).order(ByteOrder.LITTLE_ENDIAN)
        for (s in pcm) buf.putShort(s)
        out.write(buf.array())
    }
}

private fun intToByteArrayLE(i: Int): ByteArray = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array()
private fun shortToByteArrayLE(s: Short): ByteArray = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(s).array()
