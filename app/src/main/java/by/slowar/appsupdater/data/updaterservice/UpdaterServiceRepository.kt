package by.slowar.appsupdater.data.updaterservice

import by.slowar.appsupdater.data.updates.remote.AppUpdateDto
import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import io.reactivex.Observable
import io.reactivex.Single

interface UpdaterServiceRepository {

    fun checkForUpdates(packages: List<String>): Single<List<AppUpdateDto>>

    fun updateApp(packageName: String): Observable<AppUpdateItemStateDto>

    fun cancelUpdate(packageName: String): Single<Boolean>
}