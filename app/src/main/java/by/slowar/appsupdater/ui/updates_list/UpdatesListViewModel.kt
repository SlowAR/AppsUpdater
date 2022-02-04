package by.slowar.appsupdater.ui.updates_list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.slowar.appsupdater.R
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.LocalAppInfo
import by.slowar.appsupdater.data.models.UpdateAppState
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.api.AppsRepository
import by.slowar.appsupdater.domain.api.UpdaterRepository
import by.slowar.appsupdater.domain.use_cases.CheckForUpdatesUseCase
import by.slowar.appsupdater.ui.updates_list.states.AppItemUiState
import by.slowar.appsupdater.ui.updates_list.states.UpdateAppItemState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdatesListViewModel(
    private val appsRepository: AppsRepository,
    private val updaterRepository: UpdaterRepository,
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase
) : ViewModel() {

    private val _appsUiItems = MutableLiveData<List<AppItemUiState>>()
    val appsUiItems: LiveData<List<AppItemUiState>> = _appsUiItems

    private val _updatingAppState = MutableLiveData<UpdateAppItemState>()
    val updatingAppState: LiveData<UpdateAppItemState> = _updatingAppState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorStringId = MutableLiveData<Int>()
    val errorStringId: LiveData<Int> = _errorStringId

    private var installedAppsList = emptyList<LocalAppInfo>()

    private var currentRequestDisposable: Disposable? = null
    private var isRepositoryAvailable = false

    private var currentlyUpdatingAppId: Int = -1

    fun prepare() {
        _isLoading.value = true

        currentRequestDisposable = updaterRepository.init()
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .retry(3)
            .subscribe(
                { initResult ->
                    isRepositoryAvailable = initResult
                    if (initResult && installedAppsList.isEmpty()) {
                        loadInstalledAppsList()
                    } else {
                        finishLoading(IllegalStateException("Couldn't init repository"))
                    }
                },
                { error ->
                    finishLoading(error)
                }
            )
    }

    fun checkForUpdates(forceRefresh: Boolean = false) {
        currentRequestDisposable?.let { disposable ->
            if (forceRefresh) {
                disposable.dispose()
                currentRequestDisposable = null
            } else {
                return
            }
        }

        if (!isRepositoryAvailable) {
            prepare()
            return
        }

        _isLoading.value = true
        if (forceRefresh) {
            loadInstalledAppsList()
        } else {
            getAppsForUpdate()
        }
    }

    private fun getAppsForUpdate() {
        Log.e(Constants.LOG_TAG, "Checking apps for updates...")
        currentRequestDisposable = checkForUpdatesUseCase.checkForUpdates(installedAppsList)
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatesList ->
                    Log.e(
                        Constants.LOG_TAG,
                        "Apps updates checked! Apps for update: ${updatesList.size}"
                    )

                    val appsMetadata = installedAppsList.filter { appMeta ->
                        updatesList.any { appUpdate ->
                            appMeta.packageName == appUpdate.appPackage
                        }
                    }

                    val updateApps = updatesList.mapNotNull { updateItem ->
                        val appData = appsMetadata.find { app ->
                            app.packageName == updateItem.appPackage
                        }
                        appData?.let { data ->
                            AppItemUiState.IdleItemUiState(
                                appName = data.appName,
                                packageName = updateItem.appPackage,
                                description = updateItem.description,
                                updateSize = updateItem.updateSize,
                                icon = data.icon,
                                descriptionVisible = false
                            ) { updateApp(updateItem.appPackage) }
                        }
                    }

                    _appsUiItems.value = updateApps
                    finishLoading()
                },
                { error ->
                    finishLoading(error)
                }
            )
    }

    private fun loadInstalledAppsList() {
        Log.e(Constants.LOG_TAG, "Loading installed apps...")
        currentRequestDisposable = appsRepository.loadInstalledApps()
            .subscribeOn(Schedulers.single())
            .subscribe(
                {
                    Log.e(Constants.LOG_TAG, "Installed apps has been loaded! ${it.size}")
                    installedAppsList = it
                    getAppsForUpdate()
                },
                { error ->
                    finishLoading(error)
                }
            )
    }

    private fun updateApp(packageName: String) {
        //TODO fix disposables
        if (currentRequestDisposable != null) {
            return
        }

        currentRequestDisposable = updaterRepository.updateApp(packageName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { state ->
                    handleUpdateAppState(state)
                },
                { error ->
                    finishLoading(error)
                }
            )
    }

    private fun handleUpdateAppState(appUpdateState: UpdateAppState) {
        if (currentlyUpdatingAppId == -1) {
            currentlyUpdatingAppId = getAppIdByPackage(appUpdateState.packageName)
            if (currentlyUpdatingAppId == -1) {
                return
            }
        }

        //TODO remove !! - if old state is null - some error occurred, we need to send message to stop updating this app
        val oldAppUiState = _appsUiItems.value!![currentlyUpdatingAppId]
        val newAppUiState = if (appUpdateState !is UpdateAppState.ErrorState) {
            appUpdateState.toUiState(oldAppUiState)
        } else {
            AppItemUiState.IdleItemUiState(
                oldAppUiState.appName,
                oldAppUiState.packageName,
                oldAppUiState.description,
                oldAppUiState.updateSize,
                oldAppUiState.icon,
                oldAppUiState.descriptionVisible
            ) { updateApp(oldAppUiState.packageName) }
        }
        _updatingAppState.value = UpdateAppItemState(currentlyUpdatingAppId, newAppUiState)
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
        _isLoading.value = false
    }

    private fun handleError(error: Throwable) {
        _errorStringId.value = when (error) {
            else -> R.string.unknown_error
        }
        Log.e(Constants.LOG_TAG, "Error: ${error.localizedMessage}")
    }

    override fun onCleared() {
        super.onCleared()
        currentRequestDisposable?.dispose()
    }

    class Factory @Inject constructor(
        private val appsRepository: AppsRepository,
        @WorkingEntity private val updaterRepository: UpdaterRepository,
        private val checkForUpdatesUseCase: CheckForUpdatesUseCase
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdatesListViewModel(
                appsRepository,
                updaterRepository,
                checkForUpdatesUseCase
            ) as T
        }
    }
}