package com.example.notecast.utils

import kotlin.math.*

object STFTUtils {
    /**
     * Compute log-mel spectrogram:
     * returns Array<FloatArray> shape [n_mels][T]
     **/
    fun computeLogMelSpectrogram(
        samples: FloatArray,
        sampleRate: Int,
        windowSize: Int,
        hopSize: Int,
        nMels: Int = 80,
        fMin: Double = 0.0,
        fMax: Double = sampleRate / 2.0
    ): Array<FloatArray> {
        if (samples.isEmpty()) return Array(nMels) { FloatArray(0) }

        // 1) Compute STFT magnitude
        val spec = stftMagnitude(samples, windowSize, hopSize)
        val nFft = windowSize / 2 + 1
        val nFrames = spec[0].size

        // 2) Build mel filterbank (nFft bins -> nMels)
        val melFilter = melFilterBank(nFft, nMels, sampleRate, fMin, fMax, windowSize)

        // 3) Apply mel filters: mel[m][t] = sum_k filter[m][k] * mag[k][t]
        val melSpec = Array(nMels) { FloatArray(nFrames) }
        for (m in 0 until nMels) {
            val filt = melFilter[m]
            for (t in 0 until nFrames) {
                var s = 0.0
                for (k in 0 until nFft) {
                    s += filt[k] * spec[k][t]
                }
                // log scale
                melSpec[m][t] = (ln( max(1e-10, s) )).toFloat()
            }
        }
        return melSpec
    }

    private fun stftMagnitude(samples: FloatArray, win: Int, hop: Int): Array<FloatArray> {
        val nFft = win / 2 + 1
        val frames = ((samples.size - win) / hop) + 1
        if (frames <= 0) return Array(nFft) { FloatArray(0) }

        val window = hannWindow(win)
        val out = Array(nFft) { FloatArray(frames) }

        // temp arrays
        val frame = DoubleArray(win)
        val real = DoubleArray(win)
        val imag = DoubleArray(win)

        var frameIdx = 0
        var pos = 0
        while (pos + win <= samples.size) {
            for (i in 0 until win) frame[i] = samples[pos + i] * window[i]
            // compute FFT (naive O(N^2) — for better perf use FFT lib)
            fftReal(frame, real, imag)
            // magnitude for first nFft bins
            for (k in 0 until nFft) {
                val mag = sqrt(real[k]*real[k] + imag[k]*imag[k])
                out[k][frameIdx] = mag.toFloat()
            }
            frameIdx += 1
            pos += hop
        }
        return out
    }

    private fun hannWindow(n: Int): DoubleArray {
        val w = DoubleArray(n)
        for (i in 0 until n) {
            w[i] = 0.5 * (1 - cos(2.0 * Math.PI * i / (n - 1.0)))
        }
        return w
    }

    /**
     * Very small FFT implementation for real input using naive DFT for clarity.
     * This is O(N^2) — replace by optimized FFT (JTransforms or native) for production.
     */
    private fun fftReal(x: DoubleArray, realOut: DoubleArray, imagOut: DoubleArray) {
        val n = x.size
        for (k in 0 until n) {
            var re = 0.0
            var im = 0.0
            for (t in 0 until n) {
                val angle = -2.0 * Math.PI * k * t / n
                re += x[t] * cos(angle)
                im += x[t] * sin(angle)
            }
            realOut[k] = re
            imagOut[k] = im
        }
    }

    // build mel filter bank (triangular)
    private fun melFilterBank(nFft: Int, nMels: Int, sampleRate: Int, fMin: Double, fMax: Double, nWindow: Int): Array<DoubleArray> {
        val fftFreqs = DoubleArray(nFft) { i -> i.toDouble() * sampleRate / nWindow }
        val melMin = hzToMel(fMin)
        val melMax = hzToMel(fMax)
        val melPoints = DoubleArray(nMels + 2) { i -> melMin + (melMax - melMin) * i / (nMels + 1) }
        val hzPoints = DoubleArray(melPoints.size) { melToHz(melPoints[it]) }

        val bins = IntArray(hzPoints.size) { i ->
            var b = ((nWindow + 1) * hzPoints[i] / sampleRate).toInt()
            if (b < 0) b = 0
            if (b >= nFft) b = nFft - 1
            b
        }

        val fb = Array(nMels) { DoubleArray(nFft) { 0.0 } }
        for (m in 0 until nMels) {
            val fMLeft = hzPoints[m]
            val fM = hzPoints[m + 1]
            val fMRight = hzPoints[m + 2]
            val binLeft = bins[m]
            val binCenter = bins[m + 1]
            val binRight = bins[m + 2]
            for (k in 0 until nFft) {
                val freq = fftFreqs[k]
                val weight = when {
                    freq < fMLeft -> 0.0
                    freq <= fM -> (freq - fMLeft) / (fM - fMLeft)
                    freq <= fMRight -> (fMRight - freq) / (fMRight - fM)
                    else -> 0.0
                }
                fb[m][k] = weight
            }
            // normalize
            var s = fb[m].sum()
            if (s != 0.0) {
                for (k in 0 until nFft) fb[m][k] = fb[m][k] / s
            }
        }
        return fb
    }

    private fun hzToMel(hz: Double): Double = 2595.0 * log10(1.0 + hz / 700.0)
    private fun melToHz(mel: Double): Double = 700.0 * (10.0.pow(mel / 2595.0) - 1.0)
}