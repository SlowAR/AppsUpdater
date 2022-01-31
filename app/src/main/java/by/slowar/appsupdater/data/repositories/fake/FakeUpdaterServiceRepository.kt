package by.slowar.appsupdater.data.repositories.fake

import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class FakeUpdaterServiceRepository @Inject constructor() : UpdaterRepository {

    override fun init(): Observable<Boolean> {
        TODO("Not yet implemented")
    }

    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        TODO("Not yet implemented")
    }

    override fun checkForUpdates(packages: List<String>): Observable<List<UpdateAppData>> {
        TODO("Not yet implemented")
    }

    override fun updateApp(packageName: String) {
        TODO("Not yet implemented")
    }
}