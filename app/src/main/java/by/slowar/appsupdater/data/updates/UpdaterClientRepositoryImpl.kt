package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.mappers.toModel
import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.data.updates.remote.UpdaterServiceDataSourceImpl
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.updates.AppUpdate
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

@ScreenScope
class UpdaterClientRepositoryImpl @Inject constructor(
    private val updaterServiceDataSource: UpdaterServiceDataSourceImpl
) : UpdaterClientRepository {

    override fun checkForUpdates(packages: ArrayList<String>): Single<List<AppUpdate>> {
        return updaterServiceDataSource.init()
            .retry(2) { it is IllegalStateException }
            .flatMap {
                updaterServiceDataSource.checkAllAppsForUpdates(packages)
                    .map { appsList -> appsList.map { app -> app.toModel() } }
            }
    }

    override fun updateApps(packages: ArrayList<String>): Observable<AppUpdateItemStateDto> {
        return updaterServiceDataSource.updateApps(packages)
    }
}