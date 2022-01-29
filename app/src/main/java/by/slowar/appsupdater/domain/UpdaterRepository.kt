package by.slowar.appsupdater.domain

import by.slowar.appsupdater.data.models.UpdateAppData
import io.reactivex.Single

interface UpdaterRepository {

    fun checkForUpdate(packageName: String): Single<UpdateAppData>

    fun updateApp(packageName: String)
}