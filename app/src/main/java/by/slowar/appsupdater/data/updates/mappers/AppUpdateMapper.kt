package by.slowar.appsupdater.data.updates.mappers

import by.slowar.appsupdater.data.updates.remote.AppUpdateDto
import by.slowar.appsupdater.domain.installedapps.InstalledApp
import by.slowar.appsupdater.domain.updates.AppUpdate
import by.slowar.appsupdater.domain.updates.AppUpdateItem
import by.slowar.appsupdater.ui.updates.states.AppItemUiState

fun AppUpdateDto.toModel(): AppUpdate = AppUpdate(
    appPackage = this.appPackage,
    description = this.description,
    updateSize = this.updateSize
)

fun AppUpdate.toModel(metadata: InstalledApp): AppUpdateItem =
    AppUpdateItem(
        appName = metadata.appName,
        packageName = this.appPackage,
        icon = metadata.icon,
        description = this.description,
        updateSize = this.updateSize,
    )

fun AppUpdateItem.toUiState(action: () -> Unit): AppItemUiState =
    AppItemUiState.Idle(
        appName = this.appName,
        packageName = this.packageName,
        description = this.description,
        updateSize = this.updateSize,
        icon = this.icon,
        descriptionVisible = false,
        action
    )