package by.slowar.appsupdater.ui.updates_list.states

data class AppItemUiState(
    val appName: String,
    val packageName: String,
    val updateDescription: String,
    val updateSize: String,
    val isDescriptionVisible: Boolean,
    val onUpdateAction: () -> Unit
)