package com.example.notecast.core.asr

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * MelProcessor
 *
 * Trách nhiệm: PCM float 16kHz mono -> log-mel spectrogram [80, T].
 *
 * Tham số khớp với pipeline trong asr-pipeline-android.md và PhoWhisper config:
 * - sampleRate = 16_000
 * - nMels = 80 (num_mel_bins)
 * - hopLength = 160 (10 ms @16k)
 * - winLength = 400 (25 ms @16k)
 *
 * Ghi chú triển khai:
 * - STFT đơn giản với cửa sổ Hann, FFT thực bằng cách zero-pad lên power-of-two >= winLength.
 * - Power spectrogram = |FFT|^2.
 * - Mel filterbank dải [0, sampleRate/2] với thang mel HTK.
 * - Log-mel = ln(mel + eps).
 *
 * Output layout:
 * - Trả về FloatArray flatten theo [80, nFrames], tức là chỉ số: mel[f + nMels * t].
 * - Giá trị nFrames được trả kèm để encoder reshape thành [1, 80, nFrames].
 */
class MelProcessor(
    private val sampleRate: Int = 16_000,
    private val nMels: Int = 80,
    private val hopLength: Int = 160,  // 10ms @16k
    private val winLength: Int = 400,  // 25ms @16k
) {
    // FFT size: chọn power-of-two >= winLength để FFT hiệu quả hơn
    private val nFft: Int = run {
        var n = 1
        while (n < winLength) n = n shl 1
        n
    }

    // Hann window precomputed
    private val window: FloatArray = FloatArray(winLength) { i ->
        (0.5f - 0.5f * cos(2.0 * PI * i / (winLength - 1)).toFloat())
    }

    // Mel filterbank [nMels, nFft/2 + 1]
    private val melFilterBank: Array<FloatArray> = buildMelFilterBank()

    private val eps = 1e-10f

    /**
     * @param samples FloatArray 16kHz mono, giá trị [-1, 1].
     * @return Pair(flattened log-mel [80 * nFrames], nFrames)
     */
    fun computeLogMel(samples: FloatArray): Pair<FloatArray, Int> {
        if (samples.isEmpty()) return FloatArray(0) to 0

        // 1. Tính số frame STFT
        val nSamples = samples.size
        if (nSamples < winLength) {
            // Pad ngắn cho đủ một frame
            val padded = FloatArray(winLength)
            samples.copyInto(padded)
            val powerSpec = computeStftSingleFrame(padded)
            val mel = applyMelFilterbankSingle(powerSpec)
            val logMel = FloatArray(nMels) { i -> ln(mel[i] + eps) }
            return logMel to 1
        }

        val nFrames = 1 + (nSamples - winLength) / hopLength
        val powerSpec = Array(nFrames) { FloatArray(nFft / 2 + 1) }

        var frame = 0
        var offset = 0
        while (frame < nFrames) {
            // Cắt frame và áp Hann window
            val frameBuf = FloatArray(winLength)
            samples.copyInto(frameBuf, 0, offset, offset + winLength)
            for (i in 0 until winLength) {
                frameBuf[i] *= window[i]
            }
            // FFT -> power spectrum
            powerSpec[frame] = computeStftSingleFrame(frameBuf)

            frame++
            offset += hopLength
        }

        // 2. Áp dụng mel filterbank cho từng frame
        val melFrames = Array(nFrames) { FloatArray(nMels) }
        for (t in 0 until nFrames) {
            val melVec = applyMelFilterbankSingle(powerSpec[t])
            melFrames[t] = melVec
        }

        // 3. Log-mel + flatten [80, nFrames]
        val out = FloatArray(nMels * nFrames)
        for (t in 0 until nFrames) {
            val melVec = melFrames[t]
            for (m in 0 until nMels) {
                val v = ln(melVec[m] + eps)
                out[m + nMels * t] = v
            }
        }

        return out to nFrames
    }

    /**
     * Tính power spectrum cho một frame đã windowed.
     * Sử dụng FFT thực đơn giản (Cooley-Tukey) sau khi zero-pad lên nFft.
     */
    private fun computeStftSingleFrame(frame: FloatArray): FloatArray {
        // Zero-pad lên nFft
        val re = FloatArray(nFft)
        val im = FloatArray(nFft)
        val copyLen = min(frame.size, nFft)
        for (i in 0 until copyLen) re[i] = frame[i]

        // FFT in-place
        fft(re, im)

        // Power spectrum chỉ lấy 0..nFft/2
        val out = FloatArray(nFft / 2 + 1)
        for (k in 0 until out.size) {
            val r = re[k]
            val j = im[k]
            out[k] = r * r + j * j
        }
        return out
    }

    /**
     * Áp dụng mel filterbank cho một power spectrum frame.
     * powerSpec có kích thước nFft/2 + 1.
     */
    private fun applyMelFilterbankSingle(powerSpec: FloatArray): FloatArray {
        val melOut = FloatArray(nMels)
        val nFreqs = min(powerSpec.size, nFft / 2 + 1)

        for (m in 0 until nMels) {
            val filter = melFilterBank[m]
            var sum = 0f
            val len = min(filter.size, nFreqs)
            var i = 0
            while (i < len) {
                sum += filter[i] * powerSpec[i]
                i++
            }
            melOut[m] = max(sum, eps)
        }
        return melOut
    }

    /**
     * Xây dựng mel filterbank [nMels, nFft/2 + 1] dùng thang mel HTK.
     */
    private fun buildMelFilterBank(): Array<FloatArray> {
        val nFreqs = nFft / 2 + 1
        val fMin = 0.0
        val fMax = sampleRate / 2.0

        val melMin = hzToMel(fMin)
        val melMax = hzToMel(fMax)

        // nMels + 2 điểm (bao gồm 2 điểm biên cho mỗi filter tam giác)
        val melPoints = DoubleArray(nMels + 2) { i ->
            melMin + (melMax - melMin) * i / (nMels + 1)
        }
        val hzPoints = DoubleArray(melPoints.size) { i -> melToHz(melPoints[i]) }

        // Map tần số -> bin FFT
        val bin = IntArray(hzPoints.size) { i ->
            ((nFft + 1) * hzPoints[i] / sampleRate).roundToInt().coerceIn(0, nFreqs - 1)
        }

        val fb = Array(nMels) { FloatArray(nFreqs) }
        for (m in 1..nMels) {
            val fMMinus = bin[m - 1]
            val fM = bin[m]
            val fMPlus = bin[m + 1]

            if (fMMinus == fM || fM == fMPlus) continue

            // Tăng tuyến tính từ 0 -> 1
            var k = fMMinus
            while (k < fM) {
                fb[m - 1][k] = ((k - fMMinus).toFloat() / (fM - fMMinus).toFloat()).coerceAtLeast(0f)
                k++
            }
            // Giảm tuyến tính từ 1 -> 0
            k = fM
            while (k < fMPlus && k < nFreqs) {
                fb[m - 1][k] = ((fMPlus - k).toFloat() / (fMPlus - fM).toFloat()).coerceAtLeast(0f)
                k++
            }
        }

        return fb
    }

    // Chuyển Hz -> Mel (HTK)
    private fun hzToMel(hz: Double): Double = 2595.0 * ln(1.0 + hz / 700.0) / ln(10.0)

    // Chuyển Mel -> Hz (HTK)
    private fun melToHz(mel: Double): Double = 700.0 * (Math.pow(10.0, mel / 2595.0) - 1.0)

    /**
     * FFT thực đơn giản (Cooley-Tukey) in-place cho mảng float.
     * - re, im có kích thước power-of-two (nFft).
     * - Không tối ưu SIMD; đủ dùng cho khối lượng nhỏ/mid trên mobile.
     */
    private fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        require(n == im.size)

        // Bit-reversal permutation
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tmpRe = re[i]
                val tmpIm = im[i]
                re[i] = re[j]
                im[i] = im[j]
                re[j] = tmpRe
                im[j] = tmpIm
            }
        }

        // Cooley-Tukey
        var len = 2
        while (len <= n) {
            val halfLen = len / 2
            val theta = (-2.0 * PI / len).toFloat()
            val wLenRe = cos(theta)
            val wLenIm = kotlin.math.sin(theta)
            var wRe: Float
            var wIm: Float

            var i = 0
            while (i < n) {
                wRe = 1f
                wIm = 0f
                var k = 0
                while (k < halfLen) {
                    val uRe = re[i + k]
                    val uIm = im[i + k]

                    val vRe = re[i + k + halfLen] * wRe - im[i + k + halfLen] * wIm
                    val vIm = re[i + k + halfLen] * wIm + im[i + k + halfLen] * wRe

                    re[i + k] = uRe + vRe
                    im[i + k] = uIm + vIm
                    re[i + k + halfLen] = uRe - vRe
                    im[i + k + halfLen] = uIm - vIm

                    val tmpRe = wRe * wLenRe - wIm * wLenIm
                    wIm = wRe * wLenIm + wIm * wLenRe
                    wRe = tmpRe

                    k++
                }
                i += len
            }
            len = len shl 1
        }

        // Chuẩn hoá biên độ (tùy chọn: chia sqrt(n) hoặc n)
        val scale = 1.0f / sqrt(n.toFloat())
        var idx = 0
        while (idx < n) {
            re[idx] *= scale
            im[idx] *= scale
            idx++
        }
    }
}
