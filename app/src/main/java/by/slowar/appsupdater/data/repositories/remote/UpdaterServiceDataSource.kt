package by.slowar.appsupdater.data.repositories.remote

import by.slowar.appsupdater.data.models.UpdateAppData
import io.reactivex.Single
import javax.inject.Inject

class UpdaterServiceDataSource @Inject constructor() {

    fun checkAppForUpdate(packageName: String) : Single<UpdateAppData> {
        TODO()
    }

    fun updateApp(packageName: String) {
        TODO()
    }
}