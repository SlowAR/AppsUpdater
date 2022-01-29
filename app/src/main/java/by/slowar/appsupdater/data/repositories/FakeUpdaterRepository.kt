package by.slowar.appsupdater.data.repositories

import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Single

@ScreenScope
class FakeUpdaterRepository : UpdaterRepository {

    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        TODO("Not yet implemented")
    }

    override fun updateApp(packageName: String) {
        TODO("Not yet implemented")
    }
}