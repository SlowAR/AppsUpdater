package by.slowar.appsupdater.data.repositories.local

import android.content.pm.PackageManager
import by.slowar.appsupdater.data.models.LocalAppInfo
import io.reactivex.Single
import javax.inject.Inject

class InstalledAppsLocalDataSource @Inject constructor(private val packageManager: PackageManager) {

    fun loadInstalledApps(): Single<List<LocalAppInfo>> {
        TODO()
    }
}