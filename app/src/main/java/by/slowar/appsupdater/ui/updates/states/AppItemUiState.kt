package by.slowar.appsupdater.ui.updates.states

import android.graphics.drawable.Drawable
import kotlin.math.roundToInt

sealed class AppItemUiState(
    open val appName: String,
    open val packageName: String,
    open val description: String,
    open val updateSize: Long,
    open val icon: Drawable?,
    open val descriptionVisible: Boolean,
    open val taskProgressVisible: Boolean,
    open val downloadProgressVisible: Boolean,
    open val updateAvailable: Boolean,
    open val cancelUpdateAvailable: Boolean,
    open val onCancelAction: () -> Unit
) {

    object Empty : AppItemUiState(
        "", "", "", 0L, null,
        false, false, false, false,
        false, {}
    )

    data class Idle(
        override val appName: String,
        override val packageName: String,
        override val description: String,
        override val updateSize: Long,
        override val icon: Drawable?,
        override val descriptionVisible: Boolean,
        override val onCancelAction: () -> Unit,
        val onUpdateAction: () -> Unit
    ) : AppItemUiState(
        appName, packageName, description, updateSize, icon, descriptionVisible,
        false, false, true, false,
        onCancelAction
    )

    data class Pending(
        override val appName: String,
        override val packageName: String,
        override val description: String,
        override val updateSize: Long,
        override val icon: Drawable?,
        override val descriptionVisible: Boolean,
        override val onCancelAction: () -> Unit,
    ) : AppItemUiState(
        appName, packageName, description, updateSize, icon, descriptionVisible,
        true, false, false, true,
        onCancelAction
    )

    data class Initializing(
        override val appName: String,
        override val packageName: String,
        override val description: String,
        override val updateSize: Long,
        override val icon: Drawable?,
        override val descriptionVisible: Boolean,
        override val onCancelAction: () -> Unit,
    ) : AppItemUiState(
        appName, packageName, description, updateSize, icon, descriptionVisible,
        true, false, false, true,
        onCancelAction
    )

    data class Downloading(
        override val appName: String,
        override val packageName: String,
        override val description: String,
        override val updateSize: Long,
        override val icon: Drawable?,
        override val descriptionVisible: Boolean,
        override val onCancelAction: () -> Unit,
        val downloadedSize: Long = -1,
        val downloadSpeed: Long = -1,
    ) : AppItemUiState(
        appName, packageName, description, updateSize, icon, descriptionVisible,
        false, true, false, true,
        onCancelAction
    ) {

        fun getProgressPercent() = (downloadedSize.toDouble() / updateSize * 100).roundToInt()
    }

    data class Installing(
        override val appName: String,
        override val packageName: String,
        override val description: String,
        override val updateSize: Long,
        override val icon: Drawable?,
        override val descriptionVisible: Boolean,
        override val onCancelAction: () -> Unit
    ) : AppItemUiState(
        appName, packageName, description, updateSize, icon, descriptionVisible,
        true, false, false, false,
        onCancelAction
    )

    data class CompletedResult(
        override val appName: String,
        override val packageName: String,
        override val description: String,
        override val updateSize: Long,
        override val icon: Drawable?,
        override val descriptionVisible: Boolean,
        override val onCancelAction: () -> Unit
    ) : AppItemUiState(
        appName, packageName, description, updateSize, icon, descriptionVisible,
        false, false, false, false,
        onCancelAction
    )

    data class ErrorResult(
        override val appName: String,
        override val packageName: String,
        override val description: String,
        override val updateSize: Long,
        override val icon: Drawable?,
        override val descriptionVisible: Boolean,
        override val onCancelAction: () -> Unit,
        val error: Throwable
    ) : AppItemUiState(
        appName, packageName, description, updateSize, icon, descriptionVisible,
        false, false, true, false,
        onCancelAction
    )
}