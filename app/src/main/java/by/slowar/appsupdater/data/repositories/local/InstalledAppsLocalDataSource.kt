package by.slowar.appsupdater.data.repositories.local

import android.annotation.SuppressLint
import android.content.pm.PackageManager
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

                try {
                    val localAppInfo = LocalAppInfo(
                        appName = info.name,
                        packageName = info.packageName,
                        icon = packageManager.getApplicationIcon(info.packageName)
                    )
                    list.add(localAppInfo)
                } catch (e: Throwable) {
                    emitter.onError(e)
                }
            }

            emitter.onSuccess(list)
        }
    }
}