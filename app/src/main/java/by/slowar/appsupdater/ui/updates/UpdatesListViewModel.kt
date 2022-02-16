package by.slowar.appsupdater.ui.updates

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.slowar.appsupdater.R
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.updates.UpdaterRepository
import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.data.updates.toUiState
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.updates.AppUpdateItem
import by.slowar.appsupdater.domain.updates.CheckForUpdatesUseCaseImpl
import by.slowar.appsupdater.ui.updates.states.AppItemUiState
import by.slowar.appsupdater.ui.updates.states.AppUpdateItemState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdatesListViewModel(
    private val updaterRepository: UpdaterRepository,
    private val checkForUpdatesUseCase: CheckForUpdatesUseCaseImpl
) : ViewModel() {

    private val _updateResult = MutableLiveData<AppUpdateResult>()
    val updateResult: LiveData<AppUpdateResult> = _updateResult

    private val _updatingAppState = MutableLiveData<AppUpdateItemState>()
    val updatingAppState: LiveData<AppUpdateItemState> = _updatingAppState

    private var currentRequestDisposable: Disposable? = null

    private var currentlyUpdatingAppId: Int = -1

    fun checkForUpdates(forceRefresh: Boolean = false) {
        currentRequestDisposable?.let { disposable ->
            if (forceRefresh) {
                disposable.dispose()
                currentRequestDisposable = null
            } else {
                return
            }
        }

        _updateResult.value = AppUpdateResult.Loading
        currentRequestDisposable = checkForUpdatesUseCase.checkForUpdates(forceRefresh)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatesList -> handleCheckForUpdates(updatesList) },
                { error -> finishLoading(error) }
            )
    }

    private fun handleCheckForUpdates(updatesList: List<AppUpdateItem>) {
        Log.e(
            Constants.LOG_TAG,
            "Apps updates checked! Apps for update: ${updatesList.size}"
        )

        if (updatesList.isEmpty()) {
            _updateResult.value = AppUpdateResult.EmptyResult
        } else {
            val updatesListUi = updatesList.map { item ->
                item.toUiState {
                    updaterRepository.updateApp(item.packageName)
                }
            }
            _updateResult.value = AppUpdateResult.SuccessResult(updatesListUi)
        }

        finishLoading()
    }

    private fun updateApp(packageName: String) {
        if (currentRequestDisposable != null) {
            return
        }

        currentRequestDisposable = updaterRepository.updateApp(packageName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { state -> handleUpdateAppState(state) },
                { error -> finishLoading(error) }
            )
    }

    private fun handleUpdateAppState(appUpdateState: AppUpdateItemStateDto) {
        if (currentlyUpdatingAppId == -1) {
            currentlyUpdatingAppId = getAppIdByPackage(appUpdateState.packageName)
            if (currentlyUpdatingAppId == -1) {
                return
            }
        }

        //TODO remove !! - if old state is null - some error occurred, we need to send message to stop updating this app
        val oldAppUiState = _appsUiItems.value!![currentlyUpdatingAppId]
        val newAppUiState = if (appUpdateState is AppUpdateItemStateDto.ErrorResult) {
            finishLoading(appUpdateState.error)
            AppItemUiState.Idle(
                oldAppUiState.appName,
                oldAppUiState.packageName,
                oldAppUiState.description,
                oldAppUiState.updateSize,
                oldAppUiState.icon,
                oldAppUiState.descriptionVisible
            ) { updateApp(oldAppUiState.packageName) }
        } else {
            appUpdateState.toUiState(oldAppUiState)
        }

        if (newAppUiState is AppItemUiState.CompletedResult) {
            finishLoading()
        }

        _updatingAppState.value = AppUpdateItemState(currentlyUpdatingAppId, newAppUiState)
    }

    private fun getAppIdByPackage(packageName: String): Int {
        return _appsUiItems.value?.find { it.packageName == packageName }?.let {
            _appsUiItems.value?.indexOf(it)
        } ?: -1
    }

    private fun finishLoading(error: Throwable? = null) {
        if (error != null) {
            handleError(error)
        }
        currentRequestDisposable = null
    }

    private fun handleError(error: Throwable) {
        val errorStringId = when (error) {
            else -> R.string.unknown_error
        }
        Log.e(Constants.LOG_TAG, "Error: ${error.localizedMessage}")
        _updateResult.value = AppUpdateResult.ErrorResult(errorStringId)
    }

    override fun onCleared() {
        super.onCleared()
        currentRequestDisposable?.dispose()
    }

    class Factory @Inject constructor(
        @WorkingEntity private val updaterRepository: UpdaterRepository,
        private val checkForUpdatesUseCase: CheckForUpdatesUseCaseImpl
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdatesListViewModel(updaterRepository, checkForUpdatesUseCase) as T
        }
    }
}