package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.AppUpdateItemStateDto
import by.slowar.appsupdater.domain.updates.AppUpdate
import io.reactivex.Observable
import io.reactivex.Single

interface UpdaterClientRepository {

    fun checkForUpdates(packages: ArrayList<String>): Single<List<AppUpdate>>

    fun updateApps(packages: ArrayList<String>): Observable<AppUpdateItemStateDto>
}