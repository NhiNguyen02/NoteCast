package com.example.notecast.data.local

data class AudioData(
    val pcm: ShortArray = ShortArray(0),    // PCM16 samples (trimmed) as ShortArray
    val sampleRate: Int = 16000,            // sample rate in Hz
    val wavFilePath: String? = null         // Alternative: path to WAV file if available
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioData

        if (!pcm.contentEquals(other.pcm)) return false
        if (wavFilePath != other.wavFilePath) return false
        if (sampleRate != other.sampleRate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pcm.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + (wavFilePath?.hashCode() ?: 0)
        return result
    }
}
