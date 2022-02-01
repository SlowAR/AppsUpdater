package by.slowar.appsupdater.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class UpdateAppState : Parcelable {

    @Parcelize
    data class InitializeState(val data: String? = null) : UpdateAppState()

    @Parcelize
    data class DownloadingState(
        val packageName: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val downloadSpeedBytes: Long
    ) : UpdateAppState()

    @Parcelize
    data class InstallingState(val data: String? = null) : UpdateAppState()

    @Parcelize
    data class CompletedState(val data: String? = null) : UpdateAppState()

    @Parcelize
    data class ErrorState(val errorMessage: String) : UpdateAppState()
}
