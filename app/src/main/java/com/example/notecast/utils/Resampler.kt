package com.example.notecast.utils

import kotlin.math.*

// Linear resampler (simple, good enough for many cases). For higher quality use library.
object Resampler {
    fun resampleLinear(input: FloatArray, srcRate: Int, dstRate: Int): FloatArray {
        if (srcRate == dstRate) return input.copyOf()
        val ratio = dstRate.toDouble() / srcRate.toDouble()
        val outLen = max(1, (input.size * ratio).roundToInt())
        val out = FloatArray(outLen)
        for (i in 0 until outLen) {
            val srcPos = i / ratio
            val i0 = floor(srcPos).toInt()
            val i1 = min(input.size - 1, i0 + 1)
            val t = (srcPos - i0).toFloat()
            val v = input[i0] * (1f - t) + input[i1] * t
            out[i] = v
        }
        return out
    }
}
