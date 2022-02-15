package by.slowar.appsupdater.data.installedapps.local

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import by.slowar.appsupdater.common.Constants
import io.reactivex.Single
import javax.inject.Inject

interface InstalledAppsLocalDataSource {

    fun loadInstalledApps(): Single<List<InstalledAppDto>>
}

class InstalledAppsLocalDataSourceImpl @Inject constructor(
    private val packageManager: PackageManager
) : InstalledAppsLocalDataSource {

    @SuppressLint("QueryPermissionsNeeded")
    override fun loadInstalledApps(): Single<List<InstalledAppDto>> {
        return Single.create { emitter ->
            val appsInfo = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            if (appsInfo.isNullOrEmpty()) {
                emitter.onError(IllegalArgumentException("Apps info is empty!"))
                return@create
            }

            val list = ArrayList<InstalledAppDto>(appsInfo.size)
            for (info in appsInfo) {
                if (info.name == null) {
                    continue
                }
                if (emitter.isDisposed) {
                    break
                }

                try {
                    val localAppInfo = InstalledAppDto(
                        appName = info.loadLabel(packageManager).toString(),
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