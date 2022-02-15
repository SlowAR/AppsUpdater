package by.slowar.appsupdater.data.updates.remote

import android.os.Parcelable
import by.slowar.appsupdater.ui.updates.states.AppItemUiState
import kotlinx.parcelize.Parcelize

sealed class UpdateAppState(open val packageName: String) : Parcelable {

    @Parcelize
    data class InitializeState(override val packageName: String, val data: String? = null) :
        UpdateAppState(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.InitializeItemUiState {
            return AppItemUiState.InitializeItemUiState(
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
    data class DownloadingState(
        override val packageName: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val downloadSpeedBytes: Long
    ) : UpdateAppState(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.DownloadingItemUiState {
            return AppItemUiState.DownloadingItemUiState(
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
    data class InstallingState(override val packageName: String, val data: String? = null) :
        UpdateAppState(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.InstallingItemUiState {
            return AppItemUiState.InstallingItemUiState(
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
    data class CompletedState(override val packageName: String, val data: String? = null) :
        UpdateAppState(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.CompletedItemUiState {
            return AppItemUiState.CompletedItemUiState(
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
    data class ErrorState(override val packageName: String, val error: Throwable) :
        UpdateAppState(packageName) {
        override fun toUiState(oldUiState: AppItemUiState): AppItemUiState.ErrorItemUiState {
            return AppItemUiState.ErrorItemUiState(
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
