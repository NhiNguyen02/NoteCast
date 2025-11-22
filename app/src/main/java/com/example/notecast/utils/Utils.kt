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

object ModelFileUtil {
    fun copyAssetsFolder(context: Context, assetFolder: String, destDir: File) {
        if (!destDir.exists()) destDir.mkdirs()
        val assetManager = context.assets
        val files = assetManager.list(assetFolder) ?: return
        for (filename in files) {
            val inPath = "$assetFolder/$filename"
            val outFile = File(destDir, filename)
            val sub = assetManager.list(inPath)
            if (sub?.isNotEmpty() == true) {
                copyAssetsFolder(context, inPath, File(destDir, filename))
            } else {
                if (!outFile.exists()) {
                    assetManager.open(inPath).use { input ->
                        FileOutputStream(outFile).use { out ->
                            input.copyTo(out)
                        }
                    }
                }
            }
        }
    }
}