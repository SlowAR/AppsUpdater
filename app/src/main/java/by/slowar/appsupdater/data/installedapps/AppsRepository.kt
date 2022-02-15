package by.slowar.appsupdater.data.installedapps

import by.slowar.appsupdater.domain.InstalledApp
import io.reactivex.Single

interface AppsRepository {

    fun loadInstalledApps(): Single<List<InstalledApp>>
}