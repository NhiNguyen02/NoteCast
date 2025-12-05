package com.example.notecast.utils

import android.annotation.SuppressLint
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/** Timer helper **/
@SuppressLint("DefaultLocale")
fun formatElapsed(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun copyAssetToFile(context: Context, assetPath: String, outFileName: String): File {
    val outFile = File(context.filesDir, "onnx/$outFileName")
    if (!outFile.parentFile.exists()) {
        outFile.parentFile.mkdirs()
    }
    if (!outFile.exists()) {
        context.assets.open(assetPath).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
    }
    return outFile
}