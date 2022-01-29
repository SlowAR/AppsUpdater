package by.slowar.appsupdater.domain.use_cases

import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.LocalAppInfo
import by.slowar.appsupdater.data.models.UpdateAppData
import by.slowar.appsupdater.domain.api.UpdaterRepository
import io.reactivex.Single
import javax.inject.Inject

class CheckForUpdatesUseCase @Inject constructor(private val updaterRepository: UpdaterRepository) {

    fun checkForUpdates(appsList: List<LocalAppInfo>): Single<List<UpdateAppData>> {
        return Single.create { emitter ->
            val updatesAppsList = mutableListOf<UpdateAppData>()
            for (appInfo in appsList) {
                try {
                    val updateAppData =
                        updaterRepository.checkForUpdate(appInfo.packageName).blockingGet()
                    updatesAppsList.add(updateAppData)
                } catch (e: Throwable) {
                    Log.e(Constants.LOG_TAG, "checkForUpdates: ${e.localizedMessage}")
                }
            }

            emitter.onSuccess(updatesAppsList)
        }
    }
}