package com.example.notecast.core.asr

import ai.onnxruntime.OnnxTensor
import android.util.Log
import com.example.notecast.data.local.AudioData
import com.example.notecast.data.local.Transcript
import com.example.notecast.utils.Resampler
import com.example.notecast.utils.STFTUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class ASRCore(private val phoWhisper: PhoWhisperEngine) {
    private val TAG = "ASRCore"

    suspend fun transcribe(audioData: AudioData): Transcript = withContext(Dispatchers.Default) {
        Log.d(TAG, "transcribe: started. pcm.length=${audioData.pcm.size}, sampleRate=${audioData.sampleRate}")
        if (audioData.pcm.isEmpty()) {
            Log.d(TAG, "transcribe: empty pcm -> returning empty transcript")
            return@withContext Transcript("", System.currentTimeMillis())
        }

        try {
            // Step 1: normalize short -> float [-1..1]
            Log.d(TAG, "transcribe: normalizing PCM -> float")
            val floatPcm = shortToFloat(audioData.pcm)
            Log.d(TAG, "transcribe: normalized float length=${floatPcm.size}")

            // Step 2: resample to 16000 if needed
            val targetRate = 16000
            val resampled = if (audioData.sampleRate != targetRate) {
                Log.d(TAG, "transcribe: resampling from ${audioData.sampleRate} to $targetRate")
                Resampler.resampleLinear(floatPcm, audioData.sampleRate, targetRate)
            } else {
                Log.d(TAG, "transcribe: sampleRate already $targetRate, no resample")
                floatPcm
            }
            Log.d(TAG, "transcribe: resampled length=${resampled.size}")

            // Step 3: compute mel spectrogram [n_mel=80][T]
            val nMels = 80
            val hopMs = 10
            val winMs = 30
            val hop = (targetRate * hopMs / 1000.0).roundToInt()
            val win = (targetRate * winMs / 1000.0).roundToInt()

            Log.d(TAG, "transcribe: computing mel spectrogram nMels=$nMels hop=$hop win=$win")
            val mel = STFTUtils.computeLogMelSpectrogram(
                samples = resampled,
                sampleRate = targetRate,
                windowSize = win,
                hopSize = hop,
                nMels = nMels,
                fMin = 0.0,
                fMax = targetRate / 2.0
            )
            Log.d(TAG, "transcribe: mel shape = [${mel.size}][${if (mel.isNotEmpty()) mel[0].size else 0}]")
            // compute simple mel stats
            try {
                var minV = Float.MAX_VALUE
                var maxV = -Float.MAX_VALUE
                var sum = 0.0
                var count = 0
                for (r in mel) {
                    for (v in r) {
                        if (v < minV) minV = v
                        if (v > maxV) maxV = v
                        sum += v.toDouble()
                        count++
                    }
                }
                val mean = if (count > 0) sum / count else 0.0
                Log.d(TAG, "transcribe: mel stats min=$minV max=$maxV mean=$mean count=$count")
            } catch (e: Exception) {
                Log.w(TAG, "transcribe: failed to compute mel stats: ${e.message}")
            }

            // Step 4: run encoder
            Log.d(TAG, "transcribe: calling phoWhisper.runEncoder()")
            var encoderOutputs: OnnxTensor? = null
            try {
                encoderOutputs = phoWhisper.runEncoder(mel)
                Log.d(TAG, "transcribe: encoder returned info=${encoderOutputs.info}")
            } catch (e: Exception) {
                Log.e(TAG, "transcribe: encoder failed: ${e.message}")
                throw e
            }

            // Step 5: run decoder (greedy)
            Log.d(TAG, "transcribe: calling phoWhisper.runDecoderGreedy()")
            val tokens: IntArray = try {
                phoWhisper.runDecoderGreedy(encoderOutputs)
            } catch (e: Exception) {
                Log.e(TAG, "transcribe: decoder failed: ${e.message}")
                encoderOutputs.close()
                throw e
            }
            Log.d(TAG, "transcribe: decoder returned ${tokens.size} tokens")
            if (tokens.isNotEmpty()) {
                val preview = tokens.take(20).joinToString(",")
                Log.d(TAG, "transcribe: token preview (first 20) = $preview")
            }

            // Verbose decode: get raw token strings
            val (decodedText, rawPieces) = phoWhisper.decodeVerbose(tokens)
            Log.d(TAG, "transcribe: raw token strings size=${rawPieces.size} first20=${rawPieces.take(20)}")

            // Step 6: final text already decoded in decodeVerbose
            Log.d(TAG, "transcribe: decoded text='${decodedText}'")

            // cleanup encoderOutputs
            encoderOutputs.close()

            Transcript(text = decodedText, timestamp = System.currentTimeMillis())
        } catch (e: UnsupportedOperationException) {
            // phoWhisper backend not available on this device (e.g., missing kernels). Return empty transcript.
            Log.e(TAG, "phoWhisper backend unavailable: ${e.message}")
            Transcript(text = "", timestamp = System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "transcribe: unexpected error: ${e.message}", e)
            Transcript(text = "", timestamp = System.currentTimeMillis())
        }
    }

    private fun shortToFloat(shorts: ShortArray): FloatArray {
        return FloatArray(shorts.size) { i ->
            (shorts[i].toInt() / 32768f).coerceIn(-1f, 1f)
        }
    }
}
