package by.slowar.appsupdater.data.repositories

import by.slowar.appsupdater.data.models.LocalAppInfo
import by.slowar.appsupdater.data.repositories.local.InstalledAppsLocalDataSource
import by.slowar.appsupdater.domain.AppsRepository
import io.reactivex.Single
import javax.inject.Inject

class AppsRepositoryImpl @Inject constructor(private val appsSource: InstalledAppsLocalDataSource) :
    AppsRepository {

    override fun loadInstalledApps(): Single<List<LocalAppInfo>> {
        TODO("Not yet implemented")
    }
}