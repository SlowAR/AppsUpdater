package by.slowar.appsupdater.data.repositories

import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.domain.UpdaterRepository
import io.reactivex.Single

class FakeUpdaterRepository : UpdaterRepository {
    
    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        TODO("Not yet implemented")
    }

    override fun updateApp(packageName: String) {
        TODO("Not yet implemented")
    }
}