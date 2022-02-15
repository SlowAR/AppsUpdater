package by.slowar.appsupdater.data.installedapps.local

import android.graphics.drawable.Drawable

data class InstalledAppDto(
    val appName: String,
    val packageName: String,
    val icon: Drawable?
)