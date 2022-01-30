package by.slowar.appsupdater.data.repositories.data_sources.local

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.models.LocalAppInfo
import io.reactivex.Single
import javax.inject.Inject

class InstalledAppsLocalDataSource @Inject constructor(private val packageManager: PackageManager) {

    @SuppressLint("QueryPermissionsNeeded")
    fun loadInstalledApps(): Single<List<LocalAppInfo>> {
        return Single.create { emitter ->
            val appsInfo = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val list = ArrayList<LocalAppInfo>(appsInfo.size)
            for (info in appsInfo) {
                if (info.name == null) {
                    continue
                }
                if (emitter.isDisposed) {
                    break
                }

                try {
                    val localAppInfo = LocalAppInfo(
                        appName = info.name,
                        packageName = info.packageName,
                        icon = packageManager.getApplicationIcon(info.packageName)
                    )
                    list.add(localAppInfo)
                } catch (e: Throwable) {
                    Log.e(Constants.LOG_TAG, "loadInstalledApps: ${e.localizedMessage}")
                }
            }

            emitter.onSuccess(list)
        }
    }
}