package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.UpdateAppDto
import by.slowar.appsupdater.data.updates.remote.UpdateAppState
import by.slowar.appsupdater.data.updates.remote.UpdaterServiceDataSource
import by.slowar.appsupdater.di.scopes.ScreenScope
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

@ScreenScope
class UpdaterClientRepository @Inject constructor(private val remoteSource: UpdaterServiceDataSource) :
    UpdaterRepository {

    override fun init(): Observable<Boolean> {
        return remoteSource.init()
    }

    override fun checkForUpdate(packageName: String): Single<UpdateAppDto> {
        return remoteSource.checkAppForUpdate(packageName)
    }

    override fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppDto>> {
        return remoteSource.checkAllAppsForUpdates(packages)
    }

    override fun updateApp(packageName: String): Observable<UpdateAppState> {
        return remoteSource.updateApp(packageName)
    }
}