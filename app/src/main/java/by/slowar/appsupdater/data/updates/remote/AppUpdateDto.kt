package by.slowar.appsupdater.data.updates.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppUpdateDto(
    val appPackage: String,
    val description: String,
    val updateSize: Long
) : Parcelable