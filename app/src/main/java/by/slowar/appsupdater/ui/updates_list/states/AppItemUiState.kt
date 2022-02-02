package by.slowar.appsupdater.ui.updates_list.states

import android.graphics.drawable.Drawable

data class AppItemUiState(
    val appName: String,
    val packageName: String,
    val updateDescription: String,
    val statusStringId: Int = -1,
    val downloadedSize: Long = -1,
    val updateSize: Long,
    val icon: Drawable?,
    val isUpdating: Boolean = false,
    val isDescriptionVisible: Boolean,
    val onUpdateAction: () -> Unit
)