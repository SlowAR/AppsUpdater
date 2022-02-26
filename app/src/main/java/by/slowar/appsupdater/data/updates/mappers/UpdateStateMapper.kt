package by.slowar.appsupdater.data.updates.mappers

import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.ui.updates.states.AppItemUiState

fun AppUpdateItemStateDto.toUiState(metadata: AppItemUiState) =
    when (this) {
        is AppUpdateItemStateDto.Pending -> this.toUiState(metadata)
        is AppUpdateItemStateDto.Initializing -> this.toUiState(metadata)
        is AppUpdateItemStateDto.Downloading -> this.toUiState(metadata)
        is AppUpdateItemStateDto.Installing -> this.toUiState(metadata)
        is AppUpdateItemStateDto.CompletedResult -> this.toUiState(metadata)
        is AppUpdateItemStateDto.ErrorResult -> this.toUiState(metadata)
    }

fun AppItemUiState.toPendingUiState() = AppItemUiState.Pending(
    this.appName,
    this.packageName,
    this.description,
    this.updateSize,
    this.icon,
    this.descriptionVisible
)

fun AppUpdateItemStateDto.Pending.toUiState(metadata: AppItemUiState) =
    AppItemUiState.Pending(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible
    )

fun AppUpdateItemStateDto.Initializing.toUiState(metadata: AppItemUiState) =
    AppItemUiState.Initializing(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible
    )

fun AppUpdateItemStateDto.Downloading.toUiState(metadata: AppItemUiState) =
    AppItemUiState.Downloading(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
        downloadedBytes,
        downloadSpeedBytes
    )

fun AppUpdateItemStateDto.Installing.toUiState(metadata: AppItemUiState) =
    AppItemUiState.Installing(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
    )

fun AppUpdateItemStateDto.CompletedResult.toUiState(metadata: AppItemUiState) =
    AppItemUiState.CompletedResult(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
    )

fun AppUpdateItemStateDto.ErrorResult.toUiState(metadata: AppItemUiState) =
    AppItemUiState.ErrorResult(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
        error
    )