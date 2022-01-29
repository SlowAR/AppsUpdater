package by.slowar.appsupdater.ui.updates_list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.slowar.appsupdater.R
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.LocalAppInfo
import by.slowar.appsupdater.di.qualifiers.WorkingEntity
import by.slowar.appsupdater.domain.api.AppsRepository
import by.slowar.appsupdater.domain.api.UpdaterRepository
import by.slowar.appsupdater.domain.use_cases.CheckForUpdatesUseCase
import by.slowar.appsupdater.ui.updates_list.states.AppItemUiState
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorStringId = MutableLiveData<Int>()
    val errorStringId: LiveData<Int> = _errorStringId

    private var installedAppsList = listOf<LocalAppInfo>()

    private var currentRequestDisposable: Disposable? = null

    fun checkForUpdates(forceRefresh: Boolean = false) {
        if (currentRequestDisposable != null) {
            return
        }

        _isLoading.value = true
        if (installedAppsList.isEmpty() || forceRefresh) {
            loadInstalledAppsList()
        } else {
            getAppsForUpdate()
        }
    }

    private fun getAppsForUpdate() {
        currentRequestDisposable = checkForUpdatesUseCase.checkForUpdates(installedAppsList)
            .subscribeOn(Schedulers.single())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { updatesList ->
                    val updateApps = ArrayList<AppItemUiState>(updatesList.size)
                    for (updateItem in updatesList) {
                        updateApps.add(AppItemUiState(
                            appName = updateItem.name,
                            packageName = updateItem.appPackage,
                            updateDescription = updateItem.description,
                            updateSize = updateItem.updateSize,
                            icon = installedAppsList.find { app -> app.packageName == updateItem.appPackage }?.icon,
                            isDescriptionVisible = false,
                            onUpdateAction = { updaterRepository.updateApp(updateItem.appPackage) }
                        ))
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
        currentRequestDisposable = appsRepository.loadInstalledApps()
            .subscribeOn(Schedulers.single())
            .subscribe(
                {
                    installedAppsList = it
                    getAppsForUpdate()
                },
                { error ->
                    finishLoading(error)
                }
            )
    }

    private fun finishLoading(error: Throwable? = null) {
        if (error != null) {
            handleError(error)
        }
        currentRequestDisposable = null
        _isLoading.value = false
    }

    private fun handleError(error: Throwable) {
        Log.e(Constants.LOG_TAG, "loadInstalledAppsList: ${error.localizedMessage}")
        when (error) {
            else -> R.string.unknown_error
        }
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