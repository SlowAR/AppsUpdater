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
        is AppUpdateItemStateDto.CancelledResult -> throw IllegalStateException("Need to be caught earlier!")
    }

fun AppItemUiState.toIdleUiState(onUpdateAction: () -> Unit) = AppItemUiState.Idle(
    this.appName,
    this.packageName,
    this.description,
    this.updateSize,
    this.icon,
    this.descriptionVisible,
    this.onCancelAction,
    onUpdateAction
)

fun AppItemUiState.toPendingUiState() = AppItemUiState.Pending(
    this.appName,
    this.packageName,
    this.description,
    this.updateSize,
    this.icon,
    this.descriptionVisible,
    this.onCancelAction
)

fun AppUpdateItemStateDto.Pending.toUiState(metadata: AppItemUiState) =
    AppItemUiState.Pending(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
        metadata.onCancelAction
    )

fun AppUpdateItemStateDto.Initializing.toUiState(metadata: AppItemUiState) =
    AppItemUiState.Initializing(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
        metadata.onCancelAction
    )

fun AppUpdateItemStateDto.Downloading.toUiState(metadata: AppItemUiState) =
    AppItemUiState.Downloading(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
        metadata.onCancelAction,
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
        metadata.onCancelAction
    )

fun AppUpdateItemStateDto.CompletedResult.toUiState(metadata: AppItemUiState) =
    AppItemUiState.CompletedResult(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
        metadata.onCancelAction
    )

fun AppUpdateItemStateDto.ErrorResult.toUiState(metadata: AppItemUiState) =
    AppItemUiState.ErrorResult(
        metadata.appName,
        metadata.packageName,
        metadata.description,
        metadata.updateSize,
        metadata.icon,
        metadata.descriptionVisible,
        metadata.onCancelAction,
        error
    )