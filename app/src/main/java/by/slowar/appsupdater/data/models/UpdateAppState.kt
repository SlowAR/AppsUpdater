package by.slowar.appsupdater.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class UpdateAppState(open val packageName: String) : Parcelable {

    @Parcelize
    data class InitializeState(override val packageName: String, val data: String? = null) :
        UpdateAppState(packageName)

    @Parcelize
    data class DownloadingState(
        override val packageName: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val downloadSpeedBytes: Long
    ) : UpdateAppState(packageName)

    @Parcelize
    data class InstallingState(override val packageName: String, val data: String? = null) :
        UpdateAppState(packageName)

    @Parcelize
    data class CompletedState(override val packageName: String, val data: String? = null) :
        UpdateAppState(packageName)

    @Parcelize
    data class ErrorState(override val packageName: String, val errorMessage: String) :
        UpdateAppState(packageName)
}
