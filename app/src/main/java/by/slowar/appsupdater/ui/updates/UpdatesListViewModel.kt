package by.slowar.appsupdater.ui.updates

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.slowar.appsupdater.R
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.updates.UpdaterClientRepository
import by.slowar.appsupdater.data.updates.mappers.toIdleUiState
import by.slowar.appsupdater.data.updates.mappers.toPendingUiState
import by.slowar.appsupdater.data.updates.mappers.toUiState
import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.updates.AppUpdateItem
import by.slowar.appsupdater.domain.updates.CheckForUpdatesUseCaseImpl
import by.slowar.appsupdater.ui.updates.states.AppItemUiState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdatesListViewModel(
    private val updaterRepository: UpdaterClientRepository,
    private val checkForUpdatesUseCase: CheckForUpdatesUseCaseImpl
) : ViewModel() {

    private val _updateResult = MutableLiveData<AppUpdateResult>()
    val updateResult: LiveData<AppUpdateResult> = _updateResult

    private val _updatingAppState = MutableLiveData<AppItemUiState>()
    val updatingAppState: LiveData<AppItemUiState> = _updatingAppState

    private val _hasUpdatingApps = MutableLiveData<Boolean>()
    val hasUpdatingApps: LiveData<Boolean> = _hasUpdatingApps

    private var updateAppsMetadata = listOf<AppItemUiState>()
    private var updatingAppPreviousState: AppItemUiState = AppItemUiState.Empty
    private val appsForUpdateQueue = mutableListOf<String>()

    private var checkForUpdatesDisposable: Disposable? = null
    private var appUpdateDisposable: Disposable? = null
    private var cancelUpdateDisposable = CompositeDisposable()

    fun checkForUpdates(forceRefresh: Boolean = false) {
        if (appUpdateDisposable != null) {
            _updateResult.value = AppUpdateResult.Nothing
            return
        }

        checkForUpdatesDisposable?.let { disposable ->
            if (forceRefresh) {
                disposable.dispose()
                checkForUpdatesDisposable = null
            } else {
                return
            }
        }

        _updateResult.value = AppUpdateResult.Loading
        checkForUpdatesDisposable = checkForUpdatesUseCase.checkForUpdates(forceRefresh)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatesList ->
                    handleCheckForUpdates(updatesList)
                    checkForUpdatesDisposable = null
                },
                { error ->
                    handleError(error)
                    checkForUpdatesDisposable = null
                }
            )
    }

    private fun handleCheckForUpdates(updatesList: List<AppUpdateItem>) {
        if (updatesList.isEmpty()) {
            _updateResult.value = AppUpdateResult.EmptyResult
        } else {
            updateAppsMetadata = updatesList.map { item ->
                item.toUiState(
                    { cancelUpdateApp(item.packageName) },
                    { updateApp(item.packageName) }
                )
            }.toMutableList()
            _updateResult.value = AppUpdateResult.SuccessResult(updateAppsMetadata)
        }
    }

    private fun updateApp(packageName: String) {
        setPendingUiState(packageName)
        appsForUpdateQueue.add(packageName)

        if (appsForUpdateQueue.size == 1) {
            _hasUpdatingApps.value = true
        }

        updateAppsQueue()
    }

    private fun cancelUpdateApp(packageName: String) {
        val disposable = updaterRepository.cancelUpdate(packageName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isCurrentlyUpdating ->
                if (!isCurrentlyUpdating) {
                    val newUiState = updateAppsMetadata.first { state ->
                        state.packageName == packageName
                    }.toIdleUiState {
                        updateApp(packageName)
                    }
                    _updatingAppState.value = newUiState
                }
            }
        cancelUpdateDisposable.add(disposable)
    }

    private fun updateAppsQueue(echo: Boolean = false) {
        if (appUpdateDisposable != null) {
            return
        }
        if (appsForUpdateQueue.isEmpty() && !echo) {
            _hasUpdatingApps.value = false
            checkForUpdates()
            return
        }

        appUpdateDisposable = updaterRepository.updateApps(ArrayList(appsForUpdateQueue))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { state ->
                    handleUpdateAppState(state)
                },
                { error ->
                    handleError(error)
                    finishAppsUpdate()
                },
                {
                    finishAppsUpdate()
                }
            )

        if (echo) {
            appUpdateDisposable = null
        } else {
            appsForUpdateQueue.clear()
        }
    }

    fun updateAllApps() {
        if (updateAppsMetadata.isEmpty() || appUpdateDisposable != null) {
            return
        }

        _hasUpdatingApps.value = true
        setPendingUiStateAll()

        val packagesList = updateAppsMetadata.map { it.packageName } as ArrayList<String>

        appUpdateDisposable = updaterRepository.updateApps(packagesList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { state ->
                    handleUpdateAppState(state)
                },
                { error ->
                    handleError(error)
                    finishAppsUpdate()
                },
                {
                    finishAppsUpdate()
                }
            )
    }

    fun cancelAllUpdates() {
        appsForUpdateQueue.clear()
        setIdleUiStateAll()
        updaterRepository.cancelAllUpdates()
    }

    private fun setPendingUiState(packageName: String) {
        val idleStateAppId = updateAppsMetadata.indexOfFirst { it.packageName == packageName }
        _updatingAppState.value = updateAppsMetadata[idleStateAppId].toPendingUiState()
    }

    private fun setPendingUiStateAll() {
        val pendingUiStates = updateAppsMetadata.map { it.toPendingUiState() }
        _updateResult.value = AppUpdateResult.RefreshAllResult(pendingUiStates)
    }

    private fun setIdleUiStateAll() {
        val idleUiStates = updateAppsMetadata.map { pendingState ->
            pendingState.toIdleUiState {
                updateApp(pendingState.packageName)
            }
        }
        _updateResult.value = AppUpdateResult.RefreshAllResult(idleUiStates)
    }

    private fun handleUpdateAppState(appUpdateState: AppUpdateItemStateDto) {
        if (!appUpdateState.isUpdating()) {
            handleError(IllegalStateException("App need to be in updating state!"))
            return
        }

        if (updatingAppPreviousState is AppItemUiState.Empty) {
            try {
                updatingAppPreviousState =
                    updateAppsMetadata.first { appUpdateState.packageName == it.packageName }
            } catch (e: NoSuchElementException) {
                handleError(e)
                return
            }
        }

        val newAppUiState = when (appUpdateState) {
            is AppUpdateItemStateDto.ErrorResult -> {
                handleError(appUpdateState.error)
                updatingAppPreviousState.toIdleUiState {
                    updateApp(appUpdateState.packageName)
                }
            }
            is AppUpdateItemStateDto.CancelledResult -> {
                updateAppsMetadata.first { state ->
                    state.packageName == appUpdateState.packageName
                }.toIdleUiState {
                    updateApp(appUpdateState.packageName)
                }
            }
            else -> {
                appUpdateState.toUiState(updatingAppPreviousState)
            }
        }

        _updatingAppState.value = newAppUiState

        if (appUpdateState.isFinished()) {
            updatingAppPreviousState = AppItemUiState.Empty
        }
    }

    private fun handleError(error: Throwable) {
        val errorStringId = when (error) {
            else -> R.string.unknown_error
        }
        Log.e(Constants.LOG_TAG, "Error: ${error.localizedMessage}")
        _updateResult.value = AppUpdateResult.ErrorResult(errorStringId)
    }

    private fun finishAppsUpdate() {
        appUpdateDisposable = null
        updateAppsQueue()
    }

    override fun onCleared() {
        super.onCleared()
        checkForUpdatesDisposable?.dispose()
        appUpdateDisposable?.dispose()
        cancelUpdateDisposable.dispose()
    }

    class Factory @Inject constructor(
        @WorkingEntity private val updaterRepository: UpdaterClientRepository,
        private val checkForUpdatesUseCase: CheckForUpdatesUseCaseImpl
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdatesListViewModel(updaterRepository, checkForUpdatesUseCase) as T
        }
    }
}