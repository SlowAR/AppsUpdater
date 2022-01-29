package by.slowar.appsupdater.ui.updates_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.slowar.appsupdater.domain.UpdaterRepository
import by.slowar.appsupdater.ui.updates_list.states.AppItemUiState
import javax.inject.Inject

class UpdatesListViewModel(private val updaterRepository: UpdaterRepository) : ViewModel() {

    private val _appsUiItems = MutableLiveData<List<AppItemUiState>>()
    val appsUiItems: LiveData<List<AppItemUiState>> = _appsUiItems

    class Factory @Inject constructor(private val updaterRepository: UpdaterRepository) :
        ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdatesListViewModel(updaterRepository) as T
        }
    }
}