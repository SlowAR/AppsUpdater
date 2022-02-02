package by.slowar.appsupdater.utils

import android.content.Context
import by.slowar.appsupdater.ui.updates_list.states.AppItemUiState

class StringUtils {

    companion object {
        @JvmStatic
        fun formatAppStatusText(context: Context, appState: AppItemUiState): String {
            val appSize = formatBytesValue(appState.updateSize, context)
            val statusText =
                if (appState.statusStringId != -1) {
                    " - ${context.getString(appState.statusStringId)}"
                } else {
                    if (appState.downloadedSize == -1L) {
                        ""
                    } else {
                        " - ${formatBytesValue(appState.downloadedSize, context)}"
                    }
                }

            return "$statusText$appSize"
        }
    }
}