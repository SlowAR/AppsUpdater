package by.slowar.appsupdater.data.models

import android.graphics.drawable.Drawable

data class LocalAppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?
)