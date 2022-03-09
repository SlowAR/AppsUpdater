package by.slowar.appsupdater.data.installedapps

import by.slowar.appsupdater.domain.installedapps.InstalledApp
import io.reactivex.Single

interface InstalledAppsRepository {

    /**
     * Loads list of apps installed on device
     * @param forceRefresh if true - loads list of apps and cache it, else returns cached list of apps
     * @return Single of installed apps list
     */
    fun loadInstalledApps(forceRefresh: Boolean = false): Single<List<InstalledApp>>
}