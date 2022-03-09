package by.slowar.appsupdater.ui.updates

import androidx.annotation.StringRes
import by.slowar.appsupdater.ui.updates.states.AppItemUiState

sealed class AppUpdateResult {

    object Nothing : AppUpdateResult()

    object Loading : AppUpdateResult()

    object EmptyResult : AppUpdateResult()

    data class SuccessResult(val result: List<AppItemUiState>) : AppUpdateResult()

    data class ErrorResult(@StringRes val errorId: Int) : AppUpdateResult()
}