package by.slowar.appsupdater.domain.api

import by.slowar.appsupdater.data.models.LocalAppInfo
import io.reactivex.Single

interface AppsRepository {

    fun loadInstalledApps(): Single<List<LocalAppInfo>>
}