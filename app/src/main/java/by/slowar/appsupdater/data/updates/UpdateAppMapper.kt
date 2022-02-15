package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.UpdateAppDto
import by.slowar.appsupdater.data.updates.remote.UpdateAppState
import by.slowar.appsupdater.domain.updates.UpdateApp
import by.slowar.appsupdater.ui.updates.states.AppItemUiState

fun UpdateAppDto.toModel(): UpdateApp = UpdateApp(
    appPackage = this.appPackage,
    description = this.description,
    updateSize = this.updateSize
)

fun UpdateAppState.InitializeState.toUiState(oldUiState: AppItemUiState): AppItemUiState =
    AppItemUiState.InitializeItemUiState(
        oldUiState.appName,
        oldUiState.packageName,
        oldUiState.description,
        oldUiState.updateSize,
        oldUiState.icon,
        oldUiState.descriptionVisible
    )