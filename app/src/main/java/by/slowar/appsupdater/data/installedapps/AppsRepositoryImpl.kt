package by.slowar.appsupdater.data.installedapps

import by.slowar.appsupdater.data.installedapps.local.InstalledAppsLocalDataSource
import by.slowar.appsupdater.domain.installedapps.InstalledApp
import io.reactivex.Single
import javax.inject.Inject

class AppsRepositoryImpl @Inject constructor(
    private val installedAppsLocalDataSource: InstalledAppsLocalDataSource
) : AppsRepository {

    override fun loadInstalledApps(): Single<List<InstalledApp>> {
        return installedAppsLocalDataSource.loadInstalledApps()
            .map { result ->
                result.map { it.toModel() }
            }
    }
}