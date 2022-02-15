package by.slowar.appsupdater.data.updates

import by.slowar.appsupdater.data.updates.remote.UpdateAppDto
import by.slowar.appsupdater.data.updates.remote.UpdateAppState
import io.reactivex.Observable
import io.reactivex.Single

interface UpdaterRepository {

    fun init(): Observable<Boolean>

    fun checkForUpdate(packageName: String): Single<UpdateAppDto>

    fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppDto>>

    fun updateApp(packageName: String): Observable<UpdateAppState>
}