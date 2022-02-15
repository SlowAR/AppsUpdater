package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.UpdateAppState
import by.slowar.appsupdater.domain.updates.UpdateApp
import io.reactivex.Observable

interface UpdaterRepository {

    fun checkForUpdates(packages: List<String>): Observable<List<UpdateApp>>

    fun updateApp(packageName: String): Observable<UpdateAppState>
}