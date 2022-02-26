package by.slowar.appsupdater.domain.updates

import by.slowar.appsupdater.data.installedapps.InstalledAppsRepository
import by.slowar.appsupdater.data.updates.UpdaterClientRepository
import by.slowar.appsupdater.data.updates.mappers.toModel
import by.slowar.appsupdater.domain.installedapps.InstalledApp
import io.reactivex.Single
import javax.inject.Inject

interface CheckForUpdatesUseCase {

    fun checkForUpdates(forceRefresh: Boolean): Single<List<AppUpdateItem>>
}

class CheckForUpdatesUseCaseImpl @Inject constructor(
    private val updaterRepository: UpdaterClientRepository,
    private val installedAppsRepository: InstalledAppsRepository
) : CheckForUpdatesUseCase {

    override fun checkForUpdates(forceRefresh: Boolean): Single<List<AppUpdateItem>> =
        installedAppsRepository
            .loadInstalledApps(forceRefresh)
            .flatMap { installedAppsList ->
                val appUpdatesSingle = requestAppsForUpdates(installedAppsList)
                appUpdatesSingle.map { updatesList ->
                    val appsMetadata = installedAppsList.filter { appMeta ->
                        updatesList.any { appUpdate -> appMeta.packageName == appUpdate.appPackage }
                    }

                    updatesList.map { appUpdate ->
                        val app = appsMetadata.first { installedApp ->
                            installedApp.packageName == appUpdate.appPackage
                        }
                        appUpdate.toModel(app)
                    }
                }
            }

    private fun requestAppsForUpdates(installedApps: List<InstalledApp>): Single<List<AppUpdate>> {
        val packages = installedApps.map { it.packageName } as ArrayList
        return updaterRepository.checkForUpdates(packages)
    }
}