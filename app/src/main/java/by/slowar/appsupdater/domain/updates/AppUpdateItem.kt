package by.slowar.appsupdater.domain.updates

import android.graphics.drawable.Drawable

data class AppUpdateItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val description: String,
    val updateSize: Long
)