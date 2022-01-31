package by.slowar.appsupdater.utils

import android.content.Context
import by.slowar.appsupdater.R
import java.text.DecimalFormat

fun formatBytesValue(bytesValue: Long, context: Context): String {
    val formatter = DecimalFormat("#.##")

    val kilobytes = bytesValue / 1024f
    if (kilobytes < 1024) {
        val kbWord = context.getString(R.string.kilobytes_word)
        return "${formatter.format(kilobytes)}$kbWord"
    }

    val megabytes = kilobytes / 1024
    val mbWord = context.getString(R.string.megabytes_word)
    return "${formatter.format(megabytes)}$mbWord"
}