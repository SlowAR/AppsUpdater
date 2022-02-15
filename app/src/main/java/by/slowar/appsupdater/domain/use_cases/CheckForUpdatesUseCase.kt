package by.slowar.appsupdater.domain.use_cases

import by.slowar.appsupdater.data.updates.UpdaterRepository
import by.slowar.appsupdater.data.updates.remote.UpdateAppDto
import by.slowar.appsupdater.domain.InstalledApp
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class CheckForUpdatesUseCase @Inject constructor(private val updaterRepository: UpdaterRepository) {

    fun checkForUpdate(packageName: String): Single<UpdateAppDto> {
        return updaterRepository.checkForUpdate(packageName)
    }

    fun checkForUpdates(appsList: List<InstalledApp>): Observable<List<UpdateAppDto>> {
        val packages = appsList.map { it.packageName }
        return updaterRepository.checkForUpdates(packages)
    }
}