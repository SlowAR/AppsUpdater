package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.UpdateAppState
import by.slowar.appsupdater.data.updates.remote.UpdaterServiceDataSourceImpl
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.updates.UpdateApp
import io.reactivex.Observable
import javax.inject.Inject

@ScreenScope
class UpdaterClientRepository @Inject constructor(
    private val updaterServiceDataSource: UpdaterServiceDataSourceImpl
) : UpdaterRepository {

    override fun checkForUpdates(packages: List<String>): Observable<List<UpdateApp>> {
        return updaterServiceDataSource.init()
            .retry(2) { it is IllegalStateException }
            .toObservable()
            .flatMap {
                updaterServiceDataSource.checkAllAppsForUpdates(packages)
                    .map { appsList -> appsList.map { app -> app.toModel() } }
            }
    }

    override fun updateApp(packageName: String): Observable<UpdateAppState> {
        return updaterServiceDataSource.updateApp(packageName)
    }
}