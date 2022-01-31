package by.slowar.appsupdater.domain.use_cases

import by.slowar.appsupdater.data.models.LocalAppInfo
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class CheckForUpdatesUseCase @Inject constructor(private val updaterRepository: UpdaterRepository) {

    fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        return updaterRepository.checkForUpdate(packageName)
    }

    fun checkForUpdates(appsList: List<LocalAppInfo>): Observable<List<UpdateAppData>> {
        val packages = appsList.map { it.packageName }
        return updaterRepository.checkForUpdates(packages)
    }
}