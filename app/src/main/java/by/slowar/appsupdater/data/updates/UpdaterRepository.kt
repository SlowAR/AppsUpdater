package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.domain.updates.AppUpdate
import io.reactivex.Observable
import io.reactivex.Single

interface UpdaterRepository {

    fun checkForUpdates(packages: List<String>): Single<List<AppUpdate>>

    fun updateApp(packageName: String): Observable<AppUpdateItemStateDto>
}