package by.slowar.appsupdater.data.installedapps

import by.slowar.appsupdater.data.installedapps.local.InstalledAppsLocalDataSource
import by.slowar.appsupdater.domain.installedapps.InstalledApp
import io.reactivex.Single
import javax.inject.Inject

class InstalledAppsRepositoryImpl @Inject constructor(
    private val installedAppsLocalDataSource: InstalledAppsLocalDataSource
) : InstalledAppsRepository {

    var lastInstalledAppsList: List<InstalledApp> = emptyList()

    override fun loadInstalledApps(forceRefresh: Boolean): Single<List<InstalledApp>> {
        return if (forceRefresh || lastInstalledAppsList.isEmpty()) {
            installedAppsLocalDataSource.loadInstalledApps()
                .map { result ->
                    result.map { it.toModel() }
                }
                .doOnSuccess { appsList ->
                    lastInstalledAppsList = appsList
                }
        } else {
            Single.just(lastInstalledAppsList)
        }
    }
}