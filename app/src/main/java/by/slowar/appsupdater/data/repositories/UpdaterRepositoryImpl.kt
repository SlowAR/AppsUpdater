package by.slowar.appsupdater.data.repositories

import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.data.repositories.data_sources.local.UpdaterServiceDataSource
import by.slowar.appsupdater.di.scopes.ScreenScope
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Single
import javax.inject.Inject

@ScreenScope
class UpdaterRepositoryImpl @Inject constructor(private val remoteSource: UpdaterServiceDataSource) :
    UpdaterRepository {

    override fun checkForUpdate(packageName: String): Single<UpdateAppData> {
        TODO("Not yet implemented")
    }

    override fun updateApp(packageName: String) {
        Log.e(Constants.LOG_TAG, "working updateAppClick $packageName")
    }
}