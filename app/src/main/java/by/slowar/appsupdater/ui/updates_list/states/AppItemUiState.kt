package by.slowar.appsupdater.ui.updates_list.states

import android.graphics.drawable.Drawable

data class AppItemUiState(
    val appName: String,
    val packageName: String,
    val updateDescription: String,
    val updateSize: String,
    val icon: Drawable?,
    val isDescriptionVisible: Boolean,
    val onUpdateAction: () -> Unit
)