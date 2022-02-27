package by.slowar.appsupdater.data.updates.remote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AppUpdateItemStateDto(open val packageName: String) : Parcelable {

    @Parcelize
    data class Pending(override val packageName: String, val data: String? = null) :
        AppUpdateItemStateDto(packageName)

    @Parcelize
    data class Initializing(override val packageName: String, val data: String? = null) :
        AppUpdateItemStateDto(packageName)

    @Parcelize
    data class Downloading(
        override val packageName: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val downloadSpeedBytes: Long
    ) : AppUpdateItemStateDto(packageName)

    @Parcelize
    data class Installing(override val packageName: String, val data: String? = null) :
        AppUpdateItemStateDto(packageName)

    @Parcelize
    data class CompletedResult(override val packageName: String, val data: String? = null) :
        AppUpdateItemStateDto(packageName)

    @Parcelize
    data class ErrorResult(override val packageName: String, val error: Throwable) :
        AppUpdateItemStateDto(packageName)

    fun isUpdating() = this is Initializing || this is Downloading || this is Installing ||
            this is CompletedResult || this is ErrorResult
}