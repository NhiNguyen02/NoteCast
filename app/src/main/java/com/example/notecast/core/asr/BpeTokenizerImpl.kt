package com.example.notecast.core.asr

import android.content.res.AssetManager
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * BpeTokenizerImpl
 * - Tokenizer BPE theo phong cách HuggingFace ByteLevel BPE cho PhoWhisper.
 * - Đọc vocab từ vocab.json, merges từ merges.txt, special tokens từ tokenizer.json.
 * - encode: ByteLevel -> BPE merges -> thêm template BOS/lang/task/EOS.
 * - decode: map id->token, bỏ special tokens cơ bản.
 */
class BpeTokenizerImpl(
    private val vocab: Map<String, Int>,
    private val idToToken: Array<String>,
    private val merges: Map<Pair<String, String>, Int>,
    override val bosTokenId: Int,
    override val eosTokenId: Int,
    val padTokenId: Int,
    private val languageTokenId: Int?,
    private val taskTokenId: Int?,
    private val noTimestampsTokenId: Int?,
) : Tokenizer {

    // ByteLevel mapping: byte(0..255) -> unicode char, unicode char -> byte
    private val byteToUnicode: List<Char> = buildByteToUnicode()
    private val unicodeToByte: Map<Char, Int> = byteToUnicode.withIndex().associate { it.value to it.index }

    override fun encode(text: String): LongArray {
        // 1. ByteLevel pre-tokenization: string -> list<"unicode char"> (ByteLevel)
        val initialTokens = byteLevelPreTokenize(text)
        // 2. BPE merges trên dãy token này
        val bpeTokens = bpe(initialTokens)

        val ids = mutableListOf<Long>()

        // Debug: in ra các id special
        val specials = mutableListOf<Long>()
        specials += bosTokenId.toLong()
        languageTokenId?.let { specials += it.toLong() }
        taskTokenId?.let { specials += it.toLong() }
        noTimestampsTokenId?.let { specials += it.toLong() }

        Log.d("TokenizerDebug", "specials=${specials.joinToString()}")

        ids.addAll(specials)

        bpeTokens.forEach { token ->
            val id = vocab[token]
            if (id != null) {
                ids += id.toLong()
            }
        }

        ids += eosTokenId.toLong()
        return ids.toLongArray()
    }

    override fun decode(ids: LongArray): String {
        val sb = StringBuilder()
        ids.forEach { idLong ->
            val id = idLong.toInt()
            // Bỏ qua các special tokens cơ bản
            if (id == bosTokenId || id == eosTokenId || id == padTokenId ||
                id == languageTokenId || id == taskTokenId || id == noTimestampsTokenId
            ) return@forEach

            if (id >= 0 && id < idToToken.size) {
                sb.append(idToToken[id])
            }
        }
        // ByteLevel decode: chuyển lại UTF-8 bytes nếu cần
        return byteLevelDecode(sb.toString())
    }

    /**
     * ByteLevel pre-tokenization: text (UTF-8) -> list token (chuỗi 1 ký tự unicode, theo byteToUnicode).
     */
    private fun byteLevelPreTokenize(text: String): List<String> {
        val bytes = text.toByteArray(Charsets.UTF_8)
        return bytes.map { b ->
            val idx = b.toInt() and 0xFF
            byteToUnicode[idx].toString()
        }
    }

    /**
     * ByteLevel decode: từ chuỗi các token (đã ghép) -> text UTF-8.
     * Ở đây ta giả định token là các char từ bảng byteLevel.
     */
    private fun byteLevelDecode(text: String): String {
        val byteList = mutableListOf<Byte>()
        text.forEach { ch ->
            val b = unicodeToByte[ch] ?: return@forEach
            byteList += b.toByte()
        }
        return byteList.toByteArray().toString(Charsets.UTF_8)
    }

    /**
     * Thuật toán BPE chuẩn: merge theo merges (pair -> rank) cho tới khi không merge được nữa.
     */
    private fun bpe(tokens: List<String>): List<String> {
        if (tokens.size <= 1) return tokens

        var word = tokens.toMutableList()

        while (true) {
            var bestPair: Pair<Int, Pair<String, String>>? = null // (rank, pair)

            // scan tất cả cặp kề nhau
            for (i in 0 until word.size - 1) {
                val pair = word[i] to word[i + 1]
                val rank = merges[pair] ?: continue
                val currentBest = bestPair
                if (currentBest == null || rank < currentBest.first) {
                    bestPair = rank to pair
                }
            }

            val chosen = bestPair ?: break // không merge được nữa
            val (_, pairToMerge) = chosen
            val (first, second) = pairToMerge

            // merge pairToMerge thành token mới "firstsecond"
            val newWord = mutableListOf<String>()
            var i = 0
            while (i < word.size) {
                if (i < word.size - 1 && word[i] == first && word[i + 1] == second) {
                    newWord += (first + second)
                    i += 2
                } else {
                    newWord += word[i]
                    i++
                }
            }
            word = newWord
        }
        return word
    }

    /**
     * Xây bảng ByteLevel mapping giống HuggingFace (thuật toán chung, không copy code).
     * - Tập byte ban đầu: 33..126, 161..172, 174..255 -> chính nó.
     * - Các byte còn lại map sang các mã unicode từ 256 trở đi.
     */
    private fun buildByteToUnicode(): List<Char> {
        val bs = mutableListOf<Int>()
        val cs = mutableListOf<Int>()

        var i = 33
        while (i <= 126) {
            bs += i
            cs += i
            i++
        }
        i = 161
        while (i <= 172) {
            bs += i
            cs += i
            i++
        }
        i = 174
        while (i <= 255) {
            bs += i
            cs += i
            i++
        }

        var n = 0
        var j = 0
        while (bs.size < 256) {
            if (!bs.contains(j)) {
                bs += j
                cs += 256 + n
                n++
            }
            j++
        }

        return cs.map { it.toChar() }
    }

    companion object {
        private const val TAG = "BpeTokenizerImpl"
        private const val TOKENIZER_JSON = "phowhisper_base_onnx/tokenizer.json"
        private const val VOCAB_JSON = "phowhisper_base_onnx/vocab.json"
        private const val MERGES_TXT = "phowhisper_base_onnx/merges.txt"

        /**
         * Tạo instance BpeTokenizerImpl từ assets PhoWhisper.
         * - Đọc vocab.json -> vocab, idToToken
         * - Đọc merges.txt -> BPE merges
         * - Đọc tokenizer.json -> special tokens (BOS/EOS/PAD/lang/task)
         */
        fun fromAssets(assetManager: AssetManager): BpeTokenizerImpl {
            return try {
                val (vocab, idToToken) = loadVocab(assetManager)
                val merges = loadMerges(assetManager)

                val tokenizerJson = assetManager.open(TOKENIZER_JSON).use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                }
                val json = JSONObject(tokenizerJson)

                val addedTokens = json.optJSONArray("added_tokens")
                val postProcessor = json.optJSONObject("post_processor")

                var bosId: Int? = null
                var eosId: Int? = null
                var padId: Int? = null
                var langId: Int? = null
                var taskId: Int? = null
                var noTimestampsId: Int? = null

                if (addedTokens != null) {
                    for (i in 0 until addedTokens.length()) {
                        val tok = addedTokens.optJSONObject(i) ?: continue
                        val content = tok.optString("content")
                        val id = tok.optInt("id", -1)
                        val special = tok.optBoolean("special", false)
                        if (!special || id < 0) continue

                        when (content) {
                            "<|startoftranscript|>", "<|bos|>" -> bosId = id
                            "<|endoftext|>", "<|eos|>" -> eosId = id
                            "<|pad|>" -> padId = id
                            // Một số ví dụ cụ thể cho PhoWhisper
                            "<|vi|>" -> langId = id
                            "<|transcribe|>", "<|translate|>" -> if (taskId == null) taskId = id
                            "<|notimestamps|>" -> noTimestampsId = id
                        }
                    }
                }

                // Ghi chú: post_processor trong tokenizer.json có thể mô tả template chi tiết
                // (TemplateProcessing với các special tokens). Ở đây ta không parse sâu,
                // mà encode() đã dựng lại template ở mức tối thiểu như trên.
                if (postProcessor != null) {
                    // placeholder: nếu sau này cần khớp 100% với HF, có thể parse template tại đây.
                }

//                if (bosId == null || eosId == null) {
//                    val lastIdx = idToToken.size - 1
//                    if (eosId == null) eosId = lastIdx
//                    if (bosId == null) bosId = lastIdx - 1
//                    if (padId == null) padId = lastIdx - 2
//                }
                // Đảm bảo bos/eos tồn tại
                if (bosId == null || eosId == null) {
                    Log.e(
                        TAG,
                        "Tokenizer special tokens missing: bosId=$bosId, eosId=$eosId; tokenizer.json may be incompatible"
                    )
                    throw IllegalStateException("Missing BOS/EOS tokens in tokenizer.json")
                }
                // Nếu padId chưa có, gán về eosId (hoặc bosId)
                // NOTE: tokenizer.json không định nghĩa PAD;
                // hiện tại pipeline không sử dụng PAD, nên tạm thời reuse eosId để tránh crash.
                // Nếu sau này dùng batching/padding thật, cần cập nhật để tách padId riêng.
                if (padId == null) {
                    Log.w(TAG, "PAD token not found in tokenizer.json; using eosId ($eosId) as padId")
                    padId = eosId
                }
                // Log debug toàn bộ id
                Log.d(
                    TAG,
                    "Loaded tokenizer specials: bosId=$bosId, eosId=$eosId, padId=$padId, " +
                            "langId=$langId, taskId=$taskId, noTimestampsId=$noTimestampsId"
                )

                // Dùng requireNotNull để rõ ràng, không dùng trực tiếp '!!'
                val finalBosId = requireNotNull(bosId) { "bosId must not be null here" }
                val finalEosId = requireNotNull(eosId) { "eosId must not be null here" }
                val finalPadId = requireNotNull(padId) { "padId must not be null here" }

                // bosId, eosId, padId đã được đảm bảo khác null sau nhánh trên
                BpeTokenizerImpl(
                    vocab = vocab,
                    idToToken = idToToken,
                    merges = merges,
                    bosTokenId = finalBosId,
                    eosTokenId = finalEosId,
                    padTokenId = finalPadId,
                    languageTokenId = langId,
                    taskTokenId = taskId,
                    noTimestampsTokenId = noTimestampsId,
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load tokenizer from assets: ${t.message}", t)
                // Fallback very small dummy tokenizer to avoid crash; will not give good ASR.
                val dummyVocab = mapOf("" to 0)
                val idToToken = arrayOf("")
                BpeTokenizerImpl(
                    vocab = dummyVocab,
                    idToToken = idToToken,
                    merges = emptyMap(),
                    bosTokenId = 0,
                    eosTokenId = 0,
                    padTokenId = 0,
                    languageTokenId = null,
                    taskTokenId = null,
                    noTimestampsTokenId = null,
                )
            }
        }

        private fun loadVocab(assetManager: AssetManager): Pair<Map<String, Int>, Array<String>> {
            val jsonText = assetManager.open(VOCAB_JSON).use { it.bufferedReader(Charsets.UTF_8).readText() }
            val json = JSONObject(jsonText)
            val vocab = LinkedHashMap<String, Int>()

            val keys = json.keys()
            while (keys.hasNext()) {
                val token = keys.next()
                val id = json.getInt(token)
                vocab[token] = id
            }

            val size = vocab.values.maxOrNull()?.plus(1) ?: 0
            val idToToken = Array(size) { "" }
            vocab.forEach { (token, id) ->
                if (id in idToToken.indices) {
                    idToToken[id] = token
                }
            }
            return vocab to idToToken
        }

        private fun loadMerges(assetManager: AssetManager): Map<Pair<String, String>, Int> {
            val merges = LinkedHashMap<Pair<String, String>, Int>()
            assetManager.open(MERGES_TXT).use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).useLines { lines ->
                    var rank = 0
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
                        val parts = trimmed.split(" ")
                        if (parts.size != 2) continue
                        val pair = parts[0] to parts[1]
                        merges[pair] = rank++
                    }
                }
            }
            return merges
        }
    }
}
