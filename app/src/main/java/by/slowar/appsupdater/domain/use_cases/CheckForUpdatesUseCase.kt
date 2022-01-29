package by.slowar.appsupdater.domain.use_cases

import by.slowar.appsupdater.data.models.LocalAppInfo
import by.slowar.appsupdater.domain.api.UpdaterRepository
import javax.inject.Inject

class CheckForUpdatesUseCase @Inject constructor(private val updaterRepository: UpdaterRepository) {

    fun checkForUpdates(appsList: List<LocalAppInfo>) {

    }
}