package com.example.notecast.utils

import android.annotation.SuppressLint
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    // Always overwrite to ensure we use the latest model from assets
    // This is important when models are updated in assets but the app is reinstalled
    context.assets.open(assetPath).use { input ->
        FileOutputStream(outFile).use { output ->
            input.copyTo(output)
        }
    }

    return outFile
}

// Hàm tiện ích định dạng ngày tháng
fun formatNoteDate(timestamp: Long): String {
    val date = Date(timestamp)
    val todayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val currentDay = todayFormatter.format(Date())
    val noteDay = todayFormatter.format(date)

    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    return if (currentDay == noteDay) {
        "Hôm nay, ${timeFormatter.format(date)}"
    } else {
        "${noteDay}, ${timeFormatter.format(date)}"
    }
}

fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}

// Utility: preview first 500 chars
fun previewText(content: String, max: Int = 500): String {
    return if (content.length <= max) content else content.substring(0, max) + "..."
}
// Utility: extract summary from content by the marker used in ViewModel: "[Tóm tắt]:"
fun extractSummaryFromContent(content: String): String? {
    val marker = "[Tóm tắt]:"
    val idx = content.indexOf(marker)
    if (idx == -1) return null
    val after = content.substring(idx + marker.length)
    return after.trim().ifEmpty { null }
}