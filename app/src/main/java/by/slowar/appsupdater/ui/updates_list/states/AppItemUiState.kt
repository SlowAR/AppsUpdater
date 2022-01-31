package by.slowar.appsupdater.ui.updates_list.states

import android.content.Context
import android.graphics.drawable.Drawable
import by.slowar.appsupdater.utils.formatBytesValue

data class AppItemUiState(
    val appName: String,
    val packageName: String,
    val updateDescription: String,
    val updateSize: Long,
    val icon: Drawable?,
    val isDescriptionVisible: Boolean,
    val onUpdateAction: () -> Unit
) {
    fun getFormattedUpdateSize(context: Context) = formatBytesValue(updateSize, context)
}