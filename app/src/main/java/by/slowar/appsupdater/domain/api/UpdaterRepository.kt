package by.slowar.appsupdater.domain.api

import by.slowar.appsupdater.data.models.UpdateAppData
import io.reactivex.Observable
import io.reactivex.Single

interface UpdaterRepository {

    fun init(): Observable<Boolean>

    fun checkForUpdate(packageName: String): Single<UpdateAppData>

    fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppData>>

    fun updateApp(packageName: String)
}