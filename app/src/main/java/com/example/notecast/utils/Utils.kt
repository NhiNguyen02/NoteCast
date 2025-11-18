package com.example.notecast.utils

import android.annotation.SuppressLint
import java.util.concurrent.TimeUnit

/** Timer helper **/
@SuppressLint("DefaultLocale")
fun formatElapsed(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}