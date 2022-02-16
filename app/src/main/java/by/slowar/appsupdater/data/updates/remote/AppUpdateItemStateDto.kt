package by.slowar.appsupdater.data.updates.remote

import android.os.Parcelable
import by.slowar.appsupdater.ui.updates.states.AppItemUiState
import kotlinx.parcelize.Parcelize

sealed class AppUpdateItemStateDto(open val packageName: String) : Parcelable {

    @Parcelize
    data class Initializing(override val packageName: String, val data: String? = null) :
        AppUpdateItemStateDto(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.Initializing {
            return AppItemUiState.Initializing(
                oldUiState.appName,
                oldUiState.packageName,
                oldUiState.description,
                oldUiState.updateSize,
                oldUiState.icon,
                oldUiState.descriptionVisible
            )
        }
    }

    @Parcelize
    data class Downloading(
        override val packageName: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val downloadSpeedBytes: Long
    ) : AppUpdateItemStateDto(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.Downloading {
            return AppItemUiState.Downloading(
                oldUiState.appName,
                oldUiState.packageName,
                oldUiState.description,
                oldUiState.updateSize,
                oldUiState.icon,
                oldUiState.descriptionVisible,
                downloadedBytes,
                downloadSpeedBytes
            )
        }
    }

    @Parcelize
    data class Installing(override val packageName: String, val data: String? = null) :
        AppUpdateItemStateDto(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.Installing {
            return AppItemUiState.Installing(
                oldUiState.appName,
                oldUiState.packageName,
                oldUiState.description,
                oldUiState.updateSize,
                oldUiState.icon,
                oldUiState.descriptionVisible,
            )
        }
    }

    @Parcelize
    data class CompletedResult(override val packageName: String, val data: String? = null) :
        AppUpdateItemStateDto(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.CompletedResult {
            return AppItemUiState.CompletedResult(
                oldUiState.appName,
                oldUiState.packageName,
                oldUiState.description,
                oldUiState.updateSize,
                oldUiState.icon,
                oldUiState.descriptionVisible,
            )
        }
    }

    @Parcelize
    data class ErrorResult(override val packageName: String, val error: Throwable) :
        AppUpdateItemStateDto(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.ErrorResult {
            return AppItemUiState.ErrorResult(
                oldUiState.appName,
                oldUiState.packageName,
                oldUiState.description,
                oldUiState.updateSize,
                oldUiState.icon,
                oldUiState.descriptionVisible,
                error
            )
        }
    }

    abstract fun toUiState(oldUiState: AppItemUiState): AppItemUiState
}
