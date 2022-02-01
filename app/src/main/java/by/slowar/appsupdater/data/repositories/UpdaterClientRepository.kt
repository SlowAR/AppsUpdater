package by.slowar.appsupdater.data.repositories

import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.data.models.UpdateAppState
import by.slowar.appsupdater.data.repositories.data_sources.local.UpdaterServiceDataSource
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

@ScreenScope
class UpdaterClientRepository @Inject constructor(private val remoteSource: UpdaterServiceDataSource) :
    UpdaterRepository {

    override fun init(): Observable<Boolean> {
        return remoteSource.init()
    }

    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        return remoteSource.checkAppForUpdate(packageName)
    }

    override fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppData>> {
        return remoteSource.checkAllAppsForUpdates(packages)
    }

    override fun updateApp(packageName: String): Observable<UpdateAppState> {
        return remoteSource.updateApp(packageName)
    }
}