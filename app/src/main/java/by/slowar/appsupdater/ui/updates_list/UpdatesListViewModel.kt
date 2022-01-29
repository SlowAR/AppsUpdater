package by.slowar.appsupdater.ui.updates_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.slowar.appsupdater.data.models.LocalAppInfo
import by.slowar.appsupdater.domain.api.AppsRepository
import by.slowar.appsupdater.domain.use_cases.CheckForUpdatesUseCase
import by.slowar.appsupdater.ui.updates_list.states.AppItemUiState
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UpdatesListViewModel(
    private val appsRepository: AppsRepository,
    private val checkForUpdatesUseCase: CheckForUpdatesUseCase
) : ViewModel() {

    private val _appsUiItems = MutableLiveData<List<AppItemUiState>>()
    val appsUiItems: LiveData<List<AppItemUiState>> = _appsUiItems

    private var installedAppsList = listOf<LocalAppInfo>()

    fun checkForUpdates() {
        loadInstalledAppsList()
    }

    private fun loadInstalledAppsList() {
        val subscribe = appsRepository.loadInstalledApps()
            .subscribeOn(Schedulers.single())
            .subscribe(
                {
                    installedAppsList = it
                },
                {
                }
            )
    }

    class Factory @Inject constructor(
        private val appsRepository: AppsRepository,
        private val checkForUpdatesUseCase: CheckForUpdatesUseCase
    ) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdatesListViewModel(appsRepository, checkForUpdatesUseCase) as T
        }
    }
}