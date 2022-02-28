package by.slowar.appsupdater.ui.updates

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.slowar.appsupdater.R
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.updates.UpdaterClientRepository
import by.slowar.appsupdater.data.updates.mappers.toPendingUiState
import by.slowar.appsupdater.data.updates.mappers.toUiState
import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.updates.AppUpdateItem
import by.slowar.appsupdater.domain.updates.CheckForUpdatesUseCaseImpl
import by.slowar.appsupdater.ui.updates.states.AppItemUiState
import io.reactivex.android.schedulers.AndroidSchedulers
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

    private var updateAppsMetadata: MutableList<AppItemUiState> = mutableListOf()
    private var updatingAppPreviousState: AppItemUiState = AppItemUiState.Empty
    private val appsForUpdateQueue = mutableListOf<String>()

    private var checkForUpdatesDisposable: Disposable? = null
    private var appUpdateDisposable: Disposable? = null

    fun checkForUpdates(forceRefresh: Boolean = false) {
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
                { updatesList -> handleCheckForUpdates(updatesList) },
                { error -> handleError(error) }
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
            updateAppsMetadata = updatesList.map { item ->
                item.toUiState {
                    updateApp(item.packageName)
                }
            }.toMutableList()
            _updateResult.value = AppUpdateResult.SuccessResult(updateAppsMetadata)
        }
    }

    private fun updateApp(packageName: String) {
        setPendingUiState(packageName)
        appsForUpdateQueue.add(packageName)
        updateAppsQueue()
    }

    private fun updateAppsQueue() {
        if (appUpdateDisposable != null || appsForUpdateQueue.isEmpty()) {
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

        appsForUpdateQueue.clear()
    }

    fun updateAllApps() {
        if (updateAppsMetadata.isNotEmpty() && appUpdateDisposable == null) {
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
    }

    private fun setPendingUiState(packageName: String) {
        val idleStateAppId = updateAppsMetadata.indexOfFirst { it.packageName == packageName }
        _updatingAppState.value = updateAppsMetadata[idleStateAppId].toPendingUiState()
    }

    private fun setPendingUiStateAll() {
        val pendingUiStates = updateAppsMetadata.map { it.toPendingUiState() }
        _updateResult.value = AppUpdateResult.SuccessResult(pendingUiStates)
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

        val newAppUiState = if (appUpdateState is AppUpdateItemStateDto.ErrorResult) {
            handleError(appUpdateState.error)
            AppItemUiState.Idle(
                updatingAppPreviousState.appName,
                updatingAppPreviousState.packageName,
                updatingAppPreviousState.description,
                updatingAppPreviousState.updateSize,
                updatingAppPreviousState.icon,
                false
            ) { updateApp(updatingAppPreviousState.packageName) }
        } else {
            appUpdateState.toUiState(updatingAppPreviousState)
        }

        _updatingAppState.value = newAppUiState

        if (appUpdateState is AppUpdateItemStateDto.ErrorResult ||
            appUpdateState is AppUpdateItemStateDto.CompletedResult
        ) {
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