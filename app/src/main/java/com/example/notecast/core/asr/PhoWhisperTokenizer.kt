package com.example.notecast.core.asr

import android.content.Context
import org.json.JSONObject
import android.util.Log
import kotlin.collections.iterator

class PhoWhisperTokenizer(context: Context) {

    private val TAG = "PhoWhisperTokenizer"
    private val tokenToId = HashMap<String, Int>()
    private val idToTokenMap = HashMap<Int, String>()
    private val specialTokens = setOf(
        "<|startoftranscript|>",
        "<|transcribe|>",
        "<|translate|>",
        "<|notimestamps|>",
        "<|endoftext|>"
    )
    // -----------------------------
    // START / END token IDs
    // -----------------------------
    val startTokenId: Int
        get() {
            tokenToId["<|startoftranscript|>"]?.let { return it }
            tokenToId["<startoftranscript>"]?.let { return it }
            // fallback: search for any key containing 'startoftranscript' or 'start'
            val found = tokenToId.entries.find { (k, _) -> k.contains("startoftranscript", ignoreCase = true) || k.contains("start", ignoreCase = true) }
            if (found != null) {
                Log.w(TAG, "startTokenId: canonical token missing, using fallback token='${found.key}' id=${found.value}")
                return found.value
            }
            error("Missing <|startoftranscript|> token — tokenizer not loaded or vocab mismatch")
        }

    val endTokenId: Int
        get() {
            tokenToId["<|endoftext|>"]?.let { return it }
            tokenToId["<endoftext>"]?.let { return it }
            // fallback: search for any key containing 'endoftext' or 'end' substring
            val found = tokenToId.entries.find { (k, _) -> k.contains("endoftext", ignoreCase = true) || k.contains("end", ignoreCase = true) }
            if (found != null) {
                Log.w(TAG, "endTokenId: canonical token missing, using fallback token='${found.key}' id=${found.value}")
                return found.value
            }
            error("Missing <|endoftext|> token — tokenizer not loaded or vocab mismatch")
        }

    private val bytesToUnicodeMap: Map<Int, Char> by lazy { buildBytesToUnicodeMap() }
    private val unicodeToBytesMap: Map<Char, Int> by lazy { bytesToUnicodeMap.entries.associate { it.value to it.key } }

    private fun buildBytesToUnicodeMap(): Map<Int, Char> {
        // GPT-2 style reversible mapping
        val bs = mutableListOf<Int>()
        bs.addAll(33..126)   // printable ascii
        bs.addAll(161..172)
        bs.addAll(174..255)
        val cs = ArrayList<Int>()
        cs.addAll(bs)
        var n = 0
        while (bs.size < 256) {
            if (bs.contains(n)) { n++; continue }
            bs.add(n)
            cs.add(256 + cs.size)
            n++
        }
        val map = HashMap<Int, Char>()
        for (i in bs.indices) {
            map[bs[i]] = cs[i].toChar()
        }
        return map
    }

    init {
        try {
            val tokenizerJson = loadTokenizerJson(context)
            val vocabJson = try {
                loadVocabJson(context)
            } catch (e: Exception) {
                ""
            }

            // First try the newer tokenizer format
            var parsed = false
            try {
                parseTokenizerJson(tokenizerJson)
                parsed = true
            } catch (e: Exception) {
                Log.w(TAG, "parseTokenizerJson failed: ${e.message}")
            }

            // If parse failed, fallback to vocab.json + added_tokens
            if (!parsed) {
                try {
                    parseTokenizerJsonFallback(tokenizerJson, vocabJson)
                    parsed = true
                } catch (e: Exception) {
                    Log.w(TAG, "parseTokenizerJsonFallback failed: ${e.message}")
                }
            }

            // Regardless of which parse path succeeded, overlay any added_tokens present in tokenizer.json
            try {
                parseAddedTokens(tokenizerJson)
            } catch (e: Exception) {
                Log.w(TAG, "parseAddedTokens failed: ${e.message}")
            }

            // Final canonicalization of special tokens (map alternate forms)
            val altSpecials = mapOf(
                "<startoftranscript>" to "<|startoftranscript|>",
                "<endoftext>" to "<|endoftext|>",
                "<transcribe>" to "<|transcribe|>",
                "<notimestamps>" to "<|notimestamps|>",
                "<translate>" to "<|translate|>"
            )
            for ((alt, canonical) in altSpecials) {
                if (!tokenToId.containsKey(canonical) && tokenToId.containsKey(alt)) {
                    val id = tokenToId[alt]!!
                    tokenToId[canonical] = id
                    idToTokenMap[id] = canonical
                }
            }

            // New: normalize keys and map by substring matches (robust fallback)
            fun mapBySubstring(substr: String, canonical: String) {
                val foundEntry = tokenToId.entries.find { (k, _) -> k.trim().contains(substr, ignoreCase = true) }
                if (foundEntry != null) {
                    val id = foundEntry.value
                    if (!tokenToId.containsKey(canonical)) {
                        tokenToId[canonical] = id
                        idToTokenMap[id] = canonical
                        Log.w(TAG, "Mapped by substring: '$substr' -> canonical '$canonical' (found '${foundEntry.key}' -> id=$id)")
                    }
                }
            }

            mapBySubstring("startoftranscript", "<|startoftranscript|>")
            mapBySubstring("endoftext", "<|endoftext|>")
            mapBySubstring("transcribe", "<|transcribe|>")
            mapBySubstring("notimestamps", "<|notimestamps|>")
            mapBySubstring("translate", "<|translate|>")

            // Log summary of found special tokens for debugging
            Log.d(TAG, "Tokenizer init: tokenToId.size=${tokenToId.size} startId=${tokenToId["<|startoftranscript|>"]} altStart=${tokenToId["<startoftranscript>"]} endId=${tokenToId["<|endoftext|>"]} altEnd=${tokenToId["<endoftext>"]}")

            // If still missing canonical tokens, log a sample of token keys to help debugging
            if (!tokenToId.containsKey("<|startoftranscript|>") || !tokenToId.containsKey("<|endoftext|>")) {
                val sampleKeys = tokenToId.keys.take(50).joinToString(", ")
                Log.w(TAG, "Tokenizer appears missing canonical special tokens. Sample vocab keys (first 50): $sampleKeys")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize tokenizer: ${e.message}")
            throw e
        }
    }

    // -----------------------------
    // Load JSON tokenizer file
    // -----------------------------
    private fun loadTokenizerJson(context: Context): String {
        val path = "phowhisper-base-onnx/tokenizer.json"
        context.assets.open(path).use { stream ->
            return stream.readBytes().toString(Charsets.UTF_8)
        }
    }

    private fun loadVocabJson(context: Context): String {
        val path = "phowhisper-base-onnx/vocab.json"
        context.assets.open(path).use { stream ->
            return stream.readBytes().toString(Charsets.UTF_8)
        }
    }

    // -----------------------------
    // Parse tokenizer.json (newer format containing model.vocab)
    // -----------------------------
    private fun parseTokenizerJson(json: String) {
        val root = JSONObject(json)
        if (!root.has("model")) throw IllegalArgumentException("tokenizer.json missing 'model' key")
        val model = root.getJSONObject("model")
        if (!model.has("vocab")) throw IllegalArgumentException("tokenizer.json.model missing 'vocab' key")
        val vocab = model.getJSONObject("vocab")

        val iter = vocab.keys()
        while (iter.hasNext()) {
            val token = iter.next()
            val id = vocab.getInt(token)
            tokenToId[token] = id
            idToTokenMap[id] = token
        }
    }

    // -----------------------------
    // Fallback parse: vocab.json + tokenizer.json added_tokens
    // -----------------------------
    private fun parseTokenizerJsonFallback(tokenizerJson: String, vocabJson: String) {
        // Load base vocab from vocab.json (token -> id)
        val vocabRoot = JSONObject(vocabJson)
        val iter = vocabRoot.keys()
        while (iter.hasNext()) {
            val token = iter.next()
            val id = vocabRoot.getInt(token)
            tokenToId[token] = id
            idToTokenMap[id] = token
        }

        // Parse added_tokens in tokenizer.json (if present) to overlay specials
        try {
            val root = JSONObject(tokenizerJson)
            if (root.has("added_tokens")) {
                val added = root.getJSONArray("added_tokens")
                for (i in 0 until added.length()) {
                    val obj = added.getJSONObject(i)
                    val id = obj.optInt("id", -1)
                    val content = obj.optString("content", "")
                    if (id >= 0 && content.isNotEmpty()) {
                        val token = content
                        tokenToId[token] = id
                        idToTokenMap[id] = token
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "parseTokenizerJsonFallback: failed to parse added_tokens: ${e.message}")
        }
    }

    // -----------------------------
    // Parse added_tokens overlay (used regardless of main parsing path)
    // -----------------------------
    private fun parseAddedTokens(tokenizerJson: String) {
        val root = JSONObject(tokenizerJson)
        if (!root.has("added_tokens")) return
        val added = root.getJSONArray("added_tokens")
        for (i in 0 until added.length()) {
            val obj = added.getJSONObject(i)
            val id = obj.optInt("id", -1)
            val content = obj.optString("content", "")
            if (id >= 0 && content.isNotEmpty()) {
                // Prefer to store canonical form if content already uses pipe notation
                val token = content
                tokenToId[token] = id
                idToTokenMap[id] = token
                Log.d(TAG, "parseAddedTokens: added token='$token' -> id=$id")
            }
        }
    }

    // Encode text → ids (start of transcript)
    fun encodeStartOfTranscript(): IntArray {
        return intArrayOf(
            tokenToId["<|startoftranscript|>"] ?: error("Missing token"),
            tokenToId["<|transcribe|>"] ?: error("Missing token"),
            tokenToId["<|notimestamps|>"] ?: error("Missing token")
        )
    }

    // Decode ids → text
    fun decode(ids: LongArray): String {
        val tokenPieces = ArrayList<String>()
        for (idLong in ids) {
            val id = idLong.toInt()
            val token = idToTokenMap[id] ?: continue
            tokenPieces.add(token)
        }
        return decodePiecesVietnamese(tokenPieces)
    }

    private fun looksVietnameseMojibake(piece: String): Boolean {
        return piece.contains("Ã") || piece.contains("Â") || piece.contains("Ê") || piece.contains("Ă") ||
                piece.contains("áº") || piece.contains("á»") || piece.contains("â") || piece.contains("ê") ||
                piece.contains("ô") || piece.contains("ơ") || piece.contains("ư")
    }

    private fun latin1RoundTrip(piece: String): String {
        return try { String(piece.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8) } catch (_: Exception) { piece }
    }

    private fun decodePiecesVietnamese(pieces: List<String>): String {
        val sb = StringBuilder()
        for (raw in pieces) {
            if (raw.isEmpty()) continue
            if (raw in specialTokens) continue
            var p = raw
            var leadingSpace = false
            if (p.startsWith("Ġ")) {
                leadingSpace = true
                p = p.drop(1)
                if (p == "") continue
            }
            // Try latin1 fix if looks like mojibake
            if (looksVietnameseMojibake(p)) {
                p = latin1RoundTrip(p)
            }
            // Remove leftover BPE markers if any (rare) and stray control
            p = p.replace("▁", "")
            if (leadingSpace) sb.append(' ')
            sb.append(p)
        }
        var out = sb.toString()
        // Collapse repeated spaces
        out = out.replace(Regex(" +"), " ").trim()
        // Remove language token if accidentally surfaced in output
        out = out.replace("<|vi|>", "").trim()
        // Basic punctuation spacing cleanup
        out = out.replace(" ,", ",").replace(" .", ".")
        return out
    }

    // Retain old decodeTokensToText for fallback debugging if needed
    @Deprecated("Use decodePiecesVietnamese")
    private fun decodeTokensToText(tokens: List<String>): String = decodePiecesVietnamese(tokens)

    private fun normalizeText(text: String): String {
        val cleaned = text.replace("<|endoftext|>", "").replace("\n\n", "\n").trim()
        // Simple Vietnamese mojibake heuristic: common Latin-1 sequences
        val looksMojibake = cleaned.contains("Ã") || cleaned.contains("Â") || cleaned.contains("Ê") || cleaned.contains("Ă")
        return if (looksMojibake) {
            try {
                String(cleaned.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8).replace("<|endoftext|>", "").trim()
            } catch (e: Exception) { cleaned }
        } else cleaned
    }

    fun getTokenById(id: Int): String = idToTokenMap[id] ?: ""

    // -----------------------------
    // Debug / safe access helpers
    // -----------------------------
    fun findTokenIdBySubstring(substr: String): Int? {
        return tokenToId.entries.find { (k, _) -> k.contains(substr, ignoreCase = true) }?.value
    }

    fun getStartTokenIdSafe(): Int? {
        tokenToId["<|startoftranscript|>"]?.let { return it }
        tokenToId["<startoftranscript>"]?.let { return it }
        val bySub = findTokenIdBySubstring("startoftranscript") ?: findTokenIdBySubstring("start")
        if (bySub != null) {
            Log.w(TAG, "getStartTokenIdSafe: using substring match id=$bySub")
            return bySub
        }
        return null
    }

    fun getEndTokenIdSafe(): Int? {
        tokenToId["<|endoftext|>"]?.let { return it }
        tokenToId["<endoftext>"]?.let { return it }
        val bySub = findTokenIdBySubstring("endoftext") ?: findTokenIdBySubstring("endoftext") ?: findTokenIdBySubstring("end")
        if (bySub != null) {
            Log.w(TAG, "getEndTokenIdSafe: using substring match id=$bySub")
            return bySub
        }
        return null
    }

    fun getLanguageTokenIdSafe(lang: String): Int? {
        val canonical = "<|${lang}|>"
        tokenToId[canonical]?.let { return it }
        // substring match (e.g., <|vi|>)
        return tokenToId.entries.find { it.key.equals(canonical, true) || it.key.contains("<|${lang}|>", true) }?.value
    }

    fun getStartSequenceSafe(): IntArray? {
        // Try canonical full sequence first
        val start = getStartTokenIdSafe() ?: return null
        val transcribeId = tokenToId["<|transcribe|>"] ?: findTokenIdBySubstring("transcribe")
        val notimestampsId = tokenToId["<|notimestamps|>"] ?: findTokenIdBySubstring("notimestamps")
        // Attempt Vietnamese language token (vi)
        val viLang = getLanguageTokenIdSafe("vi")
        val seq = ArrayList<Int>()
        seq.add(start)
        if (viLang != null) seq.add(viLang)
        if (transcribeId != null) seq.add(transcribeId)
        if (notimestampsId != null) seq.add(notimestampsId)
        return seq.toIntArray()
    }

    fun dumpSampleKeys(limit: Int = 50): String {
        return tokenToId.keys.take(limit).joinToString(", ")
    }
}
