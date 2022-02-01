package by.slowar.appsupdater.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateAppData(val appPackage: String, val description: String, val updateSize: Long) :
    Parcelable